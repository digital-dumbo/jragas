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

public final class ContextRecallMetric implements SingleTurnEvaluationMetric {

    @Override
    public String name() {
        return "context_recall";
    }

    @Override
    public Map<MetricType, Set<String>> requiredColumns() {
        return Map.of(MetricType.SINGLE_TURN, Set.of("retrieved_contexts", "reference_contexts"));
    }

    @Override
    public MetricEvaluation score(SingleTurnSample sample) {
        Set<String> referenceTokens = unionTokens(sample.referenceContexts());
        Set<String> retrievedTokens = unionTokens(sample.retrievedContexts());

        double recall = TokenOps.overlapRatio(referenceTokens, retrievedTokens);
        return new MetricEvaluation(name(), TokenOps.clamp01(recall), "Reference-context token coverage by retrieved contexts");
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
