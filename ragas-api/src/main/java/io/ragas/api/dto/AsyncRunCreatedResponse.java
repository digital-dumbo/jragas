package io.ragas.api.dto;

import io.ragas.core.evaluation.EvaluationStatus;
import java.util.UUID;

public record AsyncRunCreatedResponse(
    UUID runId,
    EvaluationStatus status
) {
}
