package io.ragas.core.evaluation;

import io.ragas.core.metric.SingleTurnMetric;
import io.ragas.domain.dataset.SingleTurnSample;

public interface SingleTurnMetricEvaluator extends SingleTurnMetric {

    MetricEvaluation evaluate(SingleTurnSample sample);
}
