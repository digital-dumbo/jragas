package io.ragas.core.evaluation;

import java.time.Duration;

public record EvaluationRunConfig(
    int batchSize,
    Duration rowTimeout,
    int maxWorkers
) {

    public EvaluationRunConfig {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be > 0");
        }
        rowTimeout = rowTimeout == null ? Duration.ofSeconds(30) : rowTimeout;
        if (rowTimeout.isZero() || rowTimeout.isNegative()) {
            throw new IllegalArgumentException("rowTimeout must be > 0");
        }
        if (maxWorkers <= 0) {
            throw new IllegalArgumentException("maxWorkers must be > 0");
        }
    }

    public static EvaluationRunConfig defaults() {
        return new EvaluationRunConfig(10, Duration.ofSeconds(30), Math.max(1, Runtime.getRuntime().availableProcessors()));
    }
}
