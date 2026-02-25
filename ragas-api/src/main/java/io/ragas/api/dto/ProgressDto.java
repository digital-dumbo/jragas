package io.ragas.api.dto;

import io.ragas.core.evaluation.EvaluationStatus;

public record ProgressDto(
    int totalRows,
    int completedRows,
    int failedRows,
    EvaluationStatus status
) {
}
