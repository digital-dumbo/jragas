package io.ragas.core.evaluation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EvaluationRunResult(
    UUID runId,
    EvaluationStatus status,
    List<RowEvaluationResult> rows,
    Instant startedAt,
    Instant endedAt,
    String error
) {

    public EvaluationRunResult {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }

    public Duration duration() {
        if (startedAt == null || endedAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, endedAt);
    }
}
