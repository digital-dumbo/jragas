package io.ragas.metrics.result;

public record AggregatedMetric(
    String metricName,
    Object score
) {
}
