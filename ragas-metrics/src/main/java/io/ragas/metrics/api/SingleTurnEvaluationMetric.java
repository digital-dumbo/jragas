package io.ragas.metrics.api;

import io.ragas.core.evaluation.MetricEvaluation;
import io.ragas.core.evaluation.SingleTurnMetricEvaluator;
import io.ragas.domain.dataset.SingleTurnSample;

public interface SingleTurnEvaluationMetric extends SingleTurnMetricEvaluator, MetricLifecycle {

    MetricEvaluation score(SingleTurnSample sample);

    @Override
    default MetricEvaluation evaluate(SingleTurnSample sample) {
        return score(sample);
    }
}
