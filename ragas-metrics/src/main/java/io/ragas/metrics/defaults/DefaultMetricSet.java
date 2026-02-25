package io.ragas.metrics.defaults;

import io.ragas.metrics.api.SingleTurnEvaluationMetric;
import java.util.List;

public final class DefaultMetricSet {

    private DefaultMetricSet() {
    }

    public static List<SingleTurnEvaluationMetric> singleTurnDefaults() {
        return List.of(
            new AnswerRelevancyMetric(),
            new ContextPrecisionMetric(),
            new FaithfulnessMetric(),
            new ContextRecallMetric()
        );
    }
}
