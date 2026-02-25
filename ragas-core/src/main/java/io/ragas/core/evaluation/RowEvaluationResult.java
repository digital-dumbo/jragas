package io.ragas.core.evaluation;

import java.util.Map;

public record RowEvaluationResult(
    int rowIndex,
    Map<String, MetricEvaluation> metricResults,
    String error
) {

    public RowEvaluationResult {
        metricResults = metricResults == null ? Map.of() : Map.copyOf(metricResults);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
