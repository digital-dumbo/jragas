package io.ragas.metrics.defaults;

import io.ragas.core.metric.MetricType;
import io.ragas.domain.dataset.SingleTurnSample;
import io.ragas.metrics.api.SingleTurnEvaluationMetric;
import io.ragas.metrics.internal.TokenOps;
import io.ragas.core.evaluation.MetricEvaluation;
import java.util.Map;
import java.util.Set;

public final class AnswerRelevancyMetric implements SingleTurnEvaluationMetric {

    @Override
    public String name() {
        return "answer_relevancy";
    }

    @Override
    public Map<MetricType, Set<String>> requiredColumns() {
        return Map.of(MetricType.SINGLE_TURN, Set.of("user_input", "response"));
    }

    @Override
    public MetricEvaluation score(SingleTurnSample sample) {
        double score = TokenOps.jaccard(
            TokenOps.tokenize(sample.userInput()),
            TokenOps.tokenize(sample.response())
        );
        return new MetricEvaluation(name(), TokenOps.clamp01(score), "Token Jaccard overlap between user_input and response");
    }
}
