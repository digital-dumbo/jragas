package io.ragas.integrations.tracing;

import io.ragas.core.evaluation.EvaluationStatus;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingRunTraceSink implements RunTraceSink {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRunTraceSink.class);

    @Override
    public void onRunStarted(UUID runId, int rowCount) {
        LOGGER.info("ragas.run.start runId={} rows={}", runId, rowCount);
    }

    @Override
    public void onRunFinished(UUID runId, EvaluationStatus status, int processedRows, Duration duration) {
        LOGGER.info(
            "ragas.run.finish runId={} status={} processedRows={} durationMs={}",
            runId,
            status,
            processedRows,
            duration == null ? 0L : duration.toMillis()
        );
    }

    @Override
    public void onRunFailed(UUID runId, Throwable error) {
        LOGGER.error("ragas.run.error runId={} message={}", runId, error.getMessage(), error);
    }
}
