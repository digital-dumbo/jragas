package io.ragas.core.evaluation;

public record MetricEvaluation(
    String metricName,
    Object value,
    String reason
) {

    public MetricEvaluation {
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("metricName must not be blank");
        }
    }

    public boolean isNumeric() {
        return value instanceof Number;
    }

    public double numericValue() {
        if (!(value instanceof Number number)) {
            throw new IllegalStateException("Metric value is not numeric for metric: " + metricName);
        }
        return number.doubleValue();
    }
}
