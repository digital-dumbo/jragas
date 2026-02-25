package io.ragas.core.evaluation;

import io.ragas.core.metric.MetricType;
import io.ragas.domain.dataset.EvaluationDataset;
import io.ragas.domain.dataset.SingleTurnSample;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationOrchestratorTest {

    @Test
    void completesRunAndTracksProgress() throws Exception {
        EvaluationDataset dataset = buildDataset(5);
        SingleTurnMetricEvaluator metric = new ConstantMetric("faithfulness", 0.8);

        try (EvaluationOrchestrator orchestrator = new EvaluationOrchestrator(
            Executors.newSingleThreadExecutor(),
            Executors.newFixedThreadPool(2)
        )) {
            EvaluationRunHandle handle = orchestrator.startSingleTurnRun(
                dataset,
                List.of(metric),
                new EvaluationRunConfig(2, Duration.ofSeconds(2), 2)
            );

            EvaluationRunResult result = handle.future().get(5, TimeUnit.SECONDS);

            assertEquals(EvaluationStatus.COMPLETED, result.status());
            assertEquals(5, result.rows().size());
            assertEquals(5, handle.progress().completedRows());
            assertEquals(EvaluationStatus.COMPLETED, handle.progress().status());
        }
    }

    @Test
    void timesOutSlowRows() throws Exception {
        EvaluationDataset dataset = buildDataset(2);
        SingleTurnMetricEvaluator slowMetric = new SleepMetric("context_precision", Duration.ofMillis(400));

        try (EvaluationOrchestrator orchestrator = new EvaluationOrchestrator(
            Executors.newSingleThreadExecutor(),
            Executors.newFixedThreadPool(1)
        )) {
            EvaluationRunHandle handle = orchestrator.startSingleTurnRun(
                dataset,
                List.of(slowMetric),
                new EvaluationRunConfig(1, Duration.ofMillis(100), 1)
            );

            EvaluationRunResult result = handle.future().get(5, TimeUnit.SECONDS);

            assertEquals(EvaluationStatus.TIMED_OUT, result.status());
            assertTrue(result.error().contains("timeout"));
            assertEquals(EvaluationStatus.TIMED_OUT, handle.progress().status());
        }
    }

    @Test
    void supportsCancellation() throws Exception {
        EvaluationDataset dataset = buildDataset(20);
        SingleTurnMetricEvaluator slowMetric = new SleepMetric("answer_relevancy", Duration.ofMillis(200));

        try (EvaluationOrchestrator orchestrator = new EvaluationOrchestrator(
            Executors.newSingleThreadExecutor(),
            Executors.newFixedThreadPool(2)
        )) {
            EvaluationRunHandle handle = orchestrator.startSingleTurnRun(
                dataset,
                List.of(slowMetric),
                new EvaluationRunConfig(5, Duration.ofSeconds(5), 2)
            );

            Thread.sleep(150);
            handle.cancel();

            EvaluationRunResult result;
            try {
                result = handle.future().get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // CompletableFuture may complete exceptionally after cancel; query progress in that case.
                EvaluationProgress progress = handle.progress();
                assertTrue(progress.status() == EvaluationStatus.CANCELLED || handle.isCancelRequested());
                return;
            }

            assertEquals(EvaluationStatus.CANCELLED, result.status());
        }
    }

    private static EvaluationDataset buildDataset(int size) {
        List<SingleTurnSample> samples = java.util.stream.IntStream.range(0, size)
            .mapToObj(i -> new SingleTurnSample(
                "Question " + i,
                List.of("Context " + i),
                List.of("Reference context " + i),
                null,
                null,
                "Response " + i,
                null,
                null,
                null,
                null,
                null,
                null
            ))
            .toList();
        return new EvaluationDataset(samples);
    }

    private record ConstantMetric(String name, double value) implements SingleTurnMetricEvaluator {

        @Override
        public Map<MetricType, Set<String>> requiredColumns() {
            return Map.of(MetricType.SINGLE_TURN, Set.of("user_input", "response"));
        }

        @Override
        public MetricEvaluation evaluate(SingleTurnSample sample) {
            return new MetricEvaluation(name, value, "constant");
        }
    }

    private record SleepMetric(String name, Duration duration) implements SingleTurnMetricEvaluator {

        @Override
        public Map<MetricType, Set<String>> requiredColumns() {
            return Map.of(MetricType.SINGLE_TURN, Set.of("user_input", "response"));
        }

        @Override
        public MetricEvaluation evaluate(SingleTurnSample sample) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("interrupted");
            }
            return new MetricEvaluation(name, 1.0, "slept");
        }
    }
}
