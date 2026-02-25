package io.ragas.metrics.result;

import io.ragas.core.evaluation.MetricEvaluation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricAggregatorTest {

    @Test
    void aggregatesNumericScoresByMean() {
        List<MetricEvaluation> scores = List.of(
            new MetricEvaluation("faithfulness", 0.2, "a"),
            new MetricEvaluation("faithfulness", 0.8, "b")
        );

        Map<String, AggregatedMetric> aggregated = MetricAggregator.aggregate(scores);
        assertEquals(0.5, (Double) aggregated.get("faithfulness").score(), 0.0001);
    }

    @Test
    void aggregatesCategoricalScoresByFrequency() {
        List<MetricEvaluation> scores = List.of(
            new MetricEvaluation("judge_label", "pass", "a"),
            new MetricEvaluation("judge_label", "fail", "b"),
            new MetricEvaluation("judge_label", "pass", "c")
        );

        Map<String, AggregatedMetric> aggregated = MetricAggregator.aggregate(scores);
        @SuppressWarnings("unchecked")
        Map<String, Integer> frequencies = (Map<String, Integer>) aggregated.get("judge_label").score();

        assertEquals(2, frequencies.get("pass"));
        assertEquals(1, frequencies.get("fail"));
        assertTrue(frequencies.containsKey("pass"));
    }
}
