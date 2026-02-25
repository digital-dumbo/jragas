package io.ragas.core.validation;

import io.ragas.core.metric.Metric;
import io.ragas.core.metric.MetricType;
import io.ragas.core.metric.MultiTurnMetric;
import io.ragas.core.metric.SingleTurnMetric;
import io.ragas.domain.dataset.EvaluationDataset;
import io.ragas.domain.dataset.SampleType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DatasetValidation {

    private DatasetValidation() {
    }

    public static List<Map<String, Object>> remapColumnNames(
        List<Map<String, Object>> rows,
        Map<String, String> columnMap
    ) {
        Map<String, String> inverse = new HashMap<>();
        for (Map.Entry<String, String> entry : columnMap.entrySet()) {
            inverse.put(entry.getValue(), entry.getKey());
        }

        List<Map<String, Object>> remappedRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> remapped = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String canonicalName = inverse.getOrDefault(entry.getKey(), entry.getKey());
                remapped.put(canonicalName, entry.getValue());
            }
            remappedRows.add(remapped);
        }
        return remappedRows;
    }

    public static MetricType getSupportedMetricType(EvaluationDataset dataset) {
        SampleType sampleType = dataset.getSampleType();
        if (sampleType == SampleType.SINGLE_TURN) {
            return MetricType.SINGLE_TURN;
        }
        if (sampleType == SampleType.MULTI_TURN) {
            return MetricType.MULTI_TURN;
        }
        throw new IllegalArgumentException("Unsupported sample type " + sampleType);
    }

    public static void validateRequiredColumns(EvaluationDataset dataset, List<? extends Metric> metrics) {
        MetricType metricType = getSupportedMetricType(dataset);
        Set<String> availableColumns = new HashSet<>(dataset.features());

        for (Metric metric : metrics) {
            Set<String> requiredColumns = metric.normalizedRequiredColumns(metricType);
            if (!availableColumns.containsAll(requiredColumns)) {
                Set<String> missing = new HashSet<>(requiredColumns);
                missing.removeAll(availableColumns);
                throw new IllegalArgumentException(
                    "The metric [" + metric.name() + "] that is used requires the following additional columns "
                        + missing + " to be present in the dataset."
                );
            }
        }
    }

    public static void validateSupportedMetrics(EvaluationDataset dataset, List<? extends Metric> metrics) {
        SampleType sampleType = dataset.getSampleType();
        for (Metric metric : metrics) {
            boolean supported;
            if (sampleType == SampleType.SINGLE_TURN) {
                supported = metric instanceof SingleTurnMetric;
            } else if (sampleType == SampleType.MULTI_TURN) {
                supported = metric instanceof MultiTurnMetric;
            } else {
                throw new IllegalArgumentException("Unsupported sample type " + sampleType);
            }

            if (!supported) {
                throw new IllegalArgumentException(
                    "The metric '" + metric.name() + "' does not support the sample type " + sampleType + "."
                );
            }
        }
    }
}
