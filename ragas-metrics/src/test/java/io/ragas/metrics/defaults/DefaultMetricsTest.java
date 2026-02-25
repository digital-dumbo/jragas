package io.ragas.metrics.defaults;

import io.ragas.core.metric.MetricType;
import io.ragas.domain.dataset.SingleTurnSample;
import io.ragas.metrics.api.SingleTurnEvaluationMetric;
import io.ragas.core.evaluation.MetricEvaluation;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMetricsTest {

    private static final SingleTurnSample SAMPLE = new SingleTurnSample(
        "What is the capital of France?",
        List.of("Paris is the capital city of France.", "Berlin is in Germany."),
        List.of("Paris is the capital of France."),
        null,
        null,
        "The capital of France is Paris.",
        null,
        "Paris",
        null,
        null,
        null,
        null
    );

    @Test
    void defaultMetricSetContainsExpectedMetrics() {
        List<SingleTurnEvaluationMetric> metrics = DefaultMetricSet.singleTurnDefaults();
        assertEquals(4, metrics.size());
        assertEquals("answer_relevancy", metrics.get(0).name());
        assertEquals("context_precision", metrics.get(1).name());
        assertEquals("faithfulness", metrics.get(2).name());
        assertEquals("context_recall", metrics.get(3).name());
    }

    @Test
    void allDefaultMetricsProduceClampedNumericScores() {
        for (SingleTurnEvaluationMetric metric : DefaultMetricSet.singleTurnDefaults()) {
            MetricEvaluation score = metric.score(SAMPLE);
            assertTrue(score.isNumeric());
            assertTrue(score.numericValue() >= 0.0);
            assertTrue(score.numericValue() <= 1.0);
        }
    }

    @Test
    void answerRelevancyRequiresUserInputAndResponse() {
        AnswerRelevancyMetric metric = new AnswerRelevancyMetric();
        assertEquals(
            Set.of("user_input", "response"),
            metric.requiredColumns().get(MetricType.SINGLE_TURN)
        );
    }

    @Test
    void contextPrecisionIsLowerWhenNoRelevantContexts() {
        ContextPrecisionMetric metric = new ContextPrecisionMetric();

        SingleTurnSample noRelevant = new SingleTurnSample(
            "Question",
            List.of("alpha beta", "gamma delta"),
            null,
            null,
            null,
            "paris france",
            null,
            null,
            null,
            null,
            null,
            null
        );

        MetricEvaluation score = metric.score(noRelevant);
        assertEquals(0.0, score.numericValue());
    }
}
