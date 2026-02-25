package io.ragas.core.evaluation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.UUID;

public final class EvaluationRunHandle {

    private final UUID runId;
    private final CompletableFuture<EvaluationRunResult> future;
    private final Supplier<EvaluationProgress> progressSupplier;
    private final AtomicBoolean cancelRequested;

    EvaluationRunHandle(
        UUID runId,
        CompletableFuture<EvaluationRunResult> future,
        Supplier<EvaluationProgress> progressSupplier,
        AtomicBoolean cancelRequested
    ) {
        this.runId = runId;
        this.future = future;
        this.progressSupplier = progressSupplier;
        this.cancelRequested = cancelRequested;
    }

    public UUID runId() {
        return runId;
    }

    public boolean cancel() {
        cancelRequested.set(true);
        return future.cancel(true);
    }

    public boolean isCancelRequested() {
        return cancelRequested.get();
    }

    public EvaluationProgress progress() {
        return progressSupplier.get();
    }

    public CompletableFuture<EvaluationRunResult> future() {
        return future;
    }
}
