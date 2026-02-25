package io.ragas.core.metric;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface Metric {

    String name();

    default Map<MetricType, Set<String>> requiredColumns() {
        return Collections.emptyMap();
    }

    default Set<String> normalizedRequiredColumns(MetricType metricType) {
        Set<String> columns = requiredColumns().getOrDefault(metricType, Set.of());
        return columns.stream()
            .filter(column -> !column.endsWith(":ignored"))
            .map(column -> column.endsWith(":optional") ? column.substring(0, column.length() - ":optional".length()) : column)
            .collect(Collectors.toSet());
    }
}
