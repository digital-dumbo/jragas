package io.ragas.api.dto;

import io.ragas.core.evaluation.EvaluationStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record EvaluateResponse(
    UUID runId,
    EvaluationStatus status,
    List<RowResultDto> rows,
    Map<String, Object> aggregated,
    String error
) {
}
