package io.ragas.api.dto;

import io.ragas.core.evaluation.EvaluationStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RunResponse(
    UUID runId,
    EvaluationStatus status,
    ProgressDto progress,
    List<RowResultDto> rows,
    Map<String, Object> aggregated,
    String error
) {
}
