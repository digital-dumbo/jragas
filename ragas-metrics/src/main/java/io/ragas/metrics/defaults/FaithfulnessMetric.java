package io.ragas.metrics.defaults;

import io.ragas.core.metric.MetricType;
import io.ragas.domain.dataset.SingleTurnSample;
import io.ragas.metrics.api.SingleTurnEvaluationMetric;
import io.ragas.metrics.internal.TokenOps;
import io.ragas.core.evaluation.MetricEvaluation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FaithfulnessMetric implements SingleTurnEvaluationMetric {

    @Override
    public String name() {
        return "faithfulness";
    }

    @Override
    public Map<MetricType, Set<String>> requiredColumns() {
        return Map.of(MetricType.SINGLE_TURN, Set.of("response", "retrieved_contexts"));
    }

    @Override
    public MetricEvaluation score(SingleTurnSample sample) {
        Set<String> responseTokens = TokenOps.tokenize(sample.response());
        Set<String> contextTokens = unionTokens(sample.retrievedContexts());

        double groundedRatio = TokenOps.overlapRatio(responseTokens, contextTokens);
        return new MetricEvaluation(name(), TokenOps.clamp01(groundedRatio), "Response token grounding in retrieved contexts");
    }

    private static Set<String> unionTokens(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Set.of();
        }
        Set<String> union = new HashSet<>();
        for (String text : texts) {
            union.addAll(TokenOps.tokenize(text));
        }
        return union;
    }
}
