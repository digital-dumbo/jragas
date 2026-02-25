package io.ragas.metrics.api;

public interface MetricLifecycle {

    default void init() {
    }

    default void close() {
    }
}
