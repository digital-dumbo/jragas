package io.ragas.core.evaluation;

public record EvaluationProgress(
    int totalRows,
    int completedRows,
    int failedRows,
    EvaluationStatus status
) {
}
