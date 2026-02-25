package io.ragas.api.service;

import io.ragas.api.dto.AsyncRunCreatedResponse;
import io.ragas.api.dto.EvaluateRequest;
import io.ragas.api.dto.EvaluateResponse;
import io.ragas.api.dto.ProgressDto;
import io.ragas.api.dto.RowResultDto;
import io.ragas.api.dto.RunResponse;
import io.ragas.core.evaluation.EvaluationOrchestrator;
import io.ragas.core.evaluation.EvaluationProgress;
import io.ragas.core.evaluation.EvaluationRunConfig;
import io.ragas.core.evaluation.EvaluationRunHandle;
import io.ragas.core.evaluation.EvaluationRunResult;
import io.ragas.core.evaluation.EvaluationStatus;
import io.ragas.core.evaluation.MetricEvaluation;
import io.ragas.core.metric.Metric;
import io.ragas.core.validation.DatasetValidation;
import io.ragas.domain.dataset.EvaluationDataset;
import io.ragas.integrations.tracing.RunTraceSink;
import io.ragas.metrics.api.SingleTurnEvaluationMetric;
import io.ragas.metrics.defaults.DefaultMetricSet;
import io.ragas.metrics.result.AggregatedMetric;
import io.ragas.metrics.result.MetricAggregator;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class EvaluationApiService {

    private final EvaluationOrchestrator orchestrator;
    private final RunTraceSink traceSink;

    private final Map<UUID, EvaluationRunHandle> activeRuns = new ConcurrentHashMap<>();
    private final Map<UUID, EvaluationRunResult> completedRuns = new ConcurrentHashMap<>();

    public EvaluationApiService(EvaluationOrchestrator orchestrator, RunTraceSink traceSink) {
        this.orchestrator = orchestrator;
        this.traceSink = traceSink;
    }

    public EvaluateResponse evaluateSync(EvaluateRequest request) {
        EvaluationDataset dataset = EvaluationDataset.fromRows(request.datasetRows());
        List<SingleTurnEvaluationMetric> metrics = resolveMetrics(request.metrics());
        validateDataset(dataset, metrics);

        EvaluationRunHandle handle = orchestrator.startSingleTurnRun(dataset, metrics, toRunConfig(request));
        UUID runId = handle.runId();
        traceSink.onRunStarted(runId, dataset.size());

        EvaluationRunResult result;
        try {
            result = handle.future().join();
        } catch (CompletionException ex) {
            traceSink.onRunFailed(runId, ex);
            throw ex;
        }

        completedRuns.put(runId, result);
        traceSink.onRunFinished(runId, result.status(), result.rows().size(), result.duration());
        return toEvaluateResponse(result);
    }

    public AsyncRunCreatedResponse evaluateAsync(EvaluateRequest request) {
        EvaluationDataset dataset = EvaluationDataset.fromRows(request.datasetRows());
        List<SingleTurnEvaluationMetric> metrics = resolveMetrics(request.metrics());
        validateDataset(dataset, metrics);

        EvaluationRunHandle handle = orchestrator.startSingleTurnRun(dataset, metrics, toRunConfig(request));
        UUID runId = handle.runId();

        activeRuns.put(runId, handle);
        traceSink.onRunStarted(runId, dataset.size());

        handle.future().whenComplete((result, throwable) -> {
            if (throwable != null) {
                if (handle.isCancelRequested()) {
                    EvaluationRunResult cancelled = new EvaluationRunResult(
                        runId,
                        EvaluationStatus.CANCELLED,
                        List.of(),
                        Instant.now(),
                        Instant.now(),
                        "Run cancelled"
                    );
                    completedRuns.put(runId, cancelled);
                    traceSink.onRunFinished(runId, cancelled.status(), 0, cancelled.duration());
                } else {
                    Throwable root = throwable.getCause() == null ? throwable : throwable.getCause();
                    EvaluationRunResult failed = new EvaluationRunResult(
                        runId,
                        EvaluationStatus.FAILED,
                        List.of(),
                        Instant.now(),
                        Instant.now(),
                        root.getMessage()
                    );
                    completedRuns.put(runId, failed);
                    traceSink.onRunFailed(runId, root);
                }
                activeRuns.remove(runId);
                return;
            }
            completedRuns.put(runId, result);
            traceSink.onRunFinished(runId, result.status(), result.rows().size(), result.duration());
            activeRuns.remove(runId);
        });

        return new AsyncRunCreatedResponse(runId, EvaluationStatus.RUNNING);
    }

    public RunResponse getRun(UUID runId) {
        EvaluationRunResult completed = completedRuns.get(runId);
        if (completed != null) {
            EvaluationProgress progress = new EvaluationProgress(
                completed.rows().size(),
                completed.rows().size(),
                countFailures(completed),
                completed.status()
            );
            return toRunResponse(completed, progress);
        }

        EvaluationRunHandle handle = activeRuns.get(runId);
        if (handle != null) {
            EvaluationProgress progress = handle.progress();
            return new RunResponse(
                runId,
                progress.status(),
                new ProgressDto(progress.totalRows(), progress.completedRows(), progress.failedRows(), progress.status()),
                List.of(),
                Map.of(),
                null
            );
        }

        throw new IllegalArgumentException("Run not found: " + runId);
    }

    public boolean cancel(UUID runId) {
        EvaluationRunHandle handle = activeRuns.get(runId);
        return handle != null && handle.cancel();
    }

    private static void validateDataset(EvaluationDataset dataset, List<SingleTurnEvaluationMetric> metrics) {
        List<Metric> metricViews = new ArrayList<>(metrics);
        DatasetValidation.validateRequiredColumns(dataset, metricViews);
        DatasetValidation.validateSupportedMetrics(dataset, metricViews);
    }

    private static EvaluateResponse toEvaluateResponse(EvaluationRunResult result) {
        Map<String, Object> aggregated = aggregate(result.rows());
        return new EvaluateResponse(result.runId(), result.status(), toRowDtos(result.rows()), aggregated, result.error());
    }

    private static RunResponse toRunResponse(EvaluationRunResult result, EvaluationProgress progress) {
        return new RunResponse(
            result.runId(),
            result.status(),
            new ProgressDto(progress.totalRows(), progress.completedRows(), progress.failedRows(), progress.status()),
            toRowDtos(result.rows()),
            aggregate(result.rows()),
            result.error()
        );
    }

    private static List<RowResultDto> toRowDtos(List<io.ragas.core.evaluation.RowEvaluationResult> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> metricMap = new LinkedHashMap<>();
            for (Map.Entry<String, MetricEvaluation> entry : row.metricResults().entrySet()) {
                metricMap.put(entry.getKey(), entry.getValue().value());
            }
            return new RowResultDto(row.rowIndex(), metricMap, row.error());
        }).toList();
    }

    private static int countFailures(EvaluationRunResult result) {
        int failures = 0;
        for (io.ragas.core.evaluation.RowEvaluationResult row : result.rows()) {
            if (!row.isSuccess()) {
                failures++;
            }
        }
        return failures;
    }

    private static Map<String, Object> aggregate(List<io.ragas.core.evaluation.RowEvaluationResult> rows) {
        List<MetricEvaluation> values = new ArrayList<>();
        for (io.ragas.core.evaluation.RowEvaluationResult row : rows) {
            values.addAll(row.metricResults().values());
        }

        Map<String, AggregatedMetric> aggregated = MetricAggregator.aggregate(values);
        Map<String, Object> response = new LinkedHashMap<>();
        for (Map.Entry<String, AggregatedMetric> entry : aggregated.entrySet()) {
            response.put(entry.getKey(), entry.getValue().score());
        }
        return response;
    }

    private static List<SingleTurnEvaluationMetric> resolveMetrics(List<String> metricNames) {
        List<SingleTurnEvaluationMetric> defaults = DefaultMetricSet.singleTurnDefaults();
        if (metricNames == null || metricNames.isEmpty()) {
            return defaults;
        }

        Map<String, SingleTurnEvaluationMetric> registry = new LinkedHashMap<>();
        for (SingleTurnEvaluationMetric metric : defaults) {
            registry.put(metric.name(), metric);
        }

        List<SingleTurnEvaluationMetric> selected = new ArrayList<>();
        for (String metricName : metricNames) {
            SingleTurnEvaluationMetric metric = registry.get(metricName);
            if (metric == null) {
                throw new IllegalArgumentException("Unsupported metric: " + metricName);
            }
            selected.add(metric);
        }
        return selected;
    }

    private static EvaluationRunConfig toRunConfig(EvaluateRequest request) {
        EvaluationRunConfig defaults = EvaluationRunConfig.defaults();
        int batchSize = request.batchSize() == null ? defaults.batchSize() : request.batchSize();
        Duration rowTimeout = request.rowTimeoutMs() == null ? defaults.rowTimeout() : Duration.ofMillis(request.rowTimeoutMs());
        int maxWorkers = request.maxWorkers() == null ? defaults.maxWorkers() : request.maxWorkers();
        return new EvaluationRunConfig(batchSize, rowTimeout, maxWorkers);
    }
}
