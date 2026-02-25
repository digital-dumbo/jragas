package io.ragas.metrics.defaults;

import io.ragas.core.metric.MetricType;
import io.ragas.domain.dataset.SingleTurnSample;
import io.ragas.metrics.api.SingleTurnEvaluationMetric;
import io.ragas.metrics.internal.TokenOps;
import io.ragas.core.evaluation.MetricEvaluation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ContextPrecisionMetric implements SingleTurnEvaluationMetric {

    @Override
    public String name() {
        return "context_precision";
    }

    @Override
    public Map<MetricType, Set<String>> requiredColumns() {
        return Map.of(MetricType.SINGLE_TURN, Set.of("response", "retrieved_contexts"));
    }

    @Override
    public MetricEvaluation score(SingleTurnSample sample) {
        List<String> contexts = sample.retrievedContexts();
        if (contexts == null || contexts.isEmpty()) {
            return new MetricEvaluation(name(), 0.0, "No retrieved contexts available");
        }

        Set<String> responseTokens = TokenOps.tokenize(sample.response());
        int relevant = 0;
        for (String context : contexts) {
            Set<String> contextTokens = TokenOps.tokenize(context);
            if (TokenOps.overlapRatio(contextTokens, responseTokens) > 0.0) {
                relevant++;
            }
        }

        double precision = ((double) relevant) / contexts.size();
        return new MetricEvaluation(name(), TokenOps.clamp01(precision), "Fraction of retrieved contexts overlapping response tokens");
    }
}
