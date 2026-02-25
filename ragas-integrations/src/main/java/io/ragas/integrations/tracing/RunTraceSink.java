package io.ragas.integrations.tracing;

import io.ragas.core.evaluation.EvaluationStatus;
import java.time.Duration;
import java.util.UUID;

public interface RunTraceSink {

    void onRunStarted(UUID runId, int rowCount);

    void onRunFinished(UUID runId, EvaluationStatus status, int processedRows, Duration duration);

    void onRunFailed(UUID runId, Throwable error);
}
