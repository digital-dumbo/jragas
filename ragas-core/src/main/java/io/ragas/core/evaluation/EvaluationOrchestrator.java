package io.ragas.core.evaluation;

import io.ragas.domain.dataset.EvaluationDataset;
import io.ragas.domain.dataset.EvaluationSample;
import io.ragas.domain.dataset.SampleType;
import io.ragas.domain.dataset.SingleTurnSample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class EvaluationOrchestrator implements AutoCloseable {

    private final ExecutorService scheduler;
    private final ExecutorService workers;

    public EvaluationOrchestrator() {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors())));
    }

    public EvaluationOrchestrator(ExecutorService scheduler, ExecutorService workers) {
        this.scheduler = scheduler;
        this.workers = workers;
    }

    public EvaluationRunHandle startSingleTurnRun(
        EvaluationDataset dataset,
        List<? extends SingleTurnMetricEvaluator> metrics,
        EvaluationRunConfig config
    ) {
        if (dataset.getSampleType() != SampleType.SINGLE_TURN) {
            throw new IllegalArgumentException("Only SINGLE_TURN datasets are supported by this orchestrator");
        }
        if (metrics == null || metrics.isEmpty()) {
            throw new IllegalArgumentException("metrics must not be empty");
        }

        UUID runId = UUID.randomUUID();
        AtomicBoolean cancelRequested = new AtomicBoolean(false);
        AtomicInteger completedRows = new AtomicInteger(0);
        AtomicInteger failedRows = new AtomicInteger(0);
        AtomicInteger statusCode = new AtomicInteger(EvaluationStatus.PENDING.ordinal());

        CompletableFuture<EvaluationRunResult> future = CompletableFuture.supplyAsync(
            () -> runInternal(runId, dataset, metrics, config, cancelRequested, completedRows, failedRows, statusCode),
            scheduler
        );

        return new EvaluationRunHandle(
            runId,
            future,
            () -> new EvaluationProgress(
                dataset.size(),
                completedRows.get(),
                failedRows.get(),
                EvaluationStatus.values()[statusCode.get()]
            ),
            cancelRequested
        );
    }

    private EvaluationRunResult runInternal(
        UUID runId,
        EvaluationDataset dataset,
        List<? extends SingleTurnMetricEvaluator> metrics,
        EvaluationRunConfig config,
        AtomicBoolean cancelRequested,
        AtomicInteger completedRows,
        AtomicInteger failedRows,
        AtomicInteger statusCode
    ) {
        Instant startedAt = Instant.now();
        statusCode.set(EvaluationStatus.RUNNING.ordinal());

        List<RowEvaluationResult> allResults = new ArrayList<>();

        try {
            for (int offset = 0; offset < dataset.size(); offset += config.batchSize()) {
                if (cancelRequested.get()) {
                    statusCode.set(EvaluationStatus.CANCELLED.ordinal());
                    return new EvaluationRunResult(runId, EvaluationStatus.CANCELLED, allResults, startedAt, Instant.now(), null);
                }

                int end = Math.min(dataset.size(), offset + config.batchSize());
                List<Future<RowEvaluationResult>> futures = new ArrayList<>();

                for (int rowIndex = offset; rowIndex < end; rowIndex++) {
                    EvaluationSample rawSample = dataset.get(rowIndex);
                    SingleTurnSample sample = (SingleTurnSample) rawSample;
                    int finalRowIndex = rowIndex;

                    Callable<RowEvaluationResult> rowTask = () -> evaluateRow(finalRowIndex, sample, metrics);
                    futures.add(workers.submit(rowTask));
                }

                for (Future<RowEvaluationResult> rowFuture : futures) {
                    if (cancelRequested.get()) {
                        rowFuture.cancel(true);
                        continue;
                    }

                    try {
                        RowEvaluationResult result = rowFuture.get(config.rowTimeout().toMillis(), TimeUnit.MILLISECONDS);
                        allResults.add(result);
                        completedRows.incrementAndGet();
                        if (!result.isSuccess()) {
                            failedRows.incrementAndGet();
                        }
                    } catch (TimeoutException timeoutException) {
                        rowFuture.cancel(true);
                        failedRows.incrementAndGet();
                        statusCode.set(EvaluationStatus.TIMED_OUT.ordinal());
                        return new EvaluationRunResult(
                            runId,
                            EvaluationStatus.TIMED_OUT,
                            allResults,
                            startedAt,
                            Instant.now(),
                            "Row execution exceeded timeout of " + config.rowTimeout()
                        );
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        cancelRequested.set(true);
                        statusCode.set(EvaluationStatus.CANCELLED.ordinal());
                        return new EvaluationRunResult(runId, EvaluationStatus.CANCELLED, allResults, startedAt, Instant.now(), "Run interrupted");
                    } catch (ExecutionException executionException) {
                        failedRows.incrementAndGet();
                        Throwable cause = executionException.getCause() == null ? executionException : executionException.getCause();
                        allResults.add(new RowEvaluationResult(-1, Map.of(), cause.getMessage()));
                    }
                }
            }

            EvaluationStatus finalStatus = cancelRequested.get() ? EvaluationStatus.CANCELLED : EvaluationStatus.COMPLETED;
            statusCode.set(finalStatus.ordinal());
            return new EvaluationRunResult(runId, finalStatus, allResults, startedAt, Instant.now(), null);
        } catch (RuntimeException exception) {
            statusCode.set(EvaluationStatus.FAILED.ordinal());
            throw new CompletionException(exception);
        }
    }

    private static RowEvaluationResult evaluateRow(
        int rowIndex,
        SingleTurnSample sample,
        List<? extends SingleTurnMetricEvaluator> metrics
    ) {
        Map<String, MetricEvaluation> results = new LinkedHashMap<>();
        for (SingleTurnMetricEvaluator metric : metrics) {
            MetricEvaluation metricEvaluation = metric.evaluate(sample);
            results.put(metric.name(), metricEvaluation);
        }
        return new RowEvaluationResult(rowIndex, results, null);
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
        workers.shutdownNow();
    }
}
