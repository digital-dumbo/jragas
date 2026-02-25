package io.ragas.metrics.result;

import io.ragas.core.evaluation.MetricEvaluation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MetricAggregator {

    private MetricAggregator() {
    }

    public static Map<String, AggregatedMetric> aggregate(List<MetricEvaluation> scores) {
        Map<String, List<MetricEvaluation>> grouped = new LinkedHashMap<>();
        for (MetricEvaluation score : scores) {
            grouped.computeIfAbsent(score.metricName(), ignored -> new java.util.ArrayList<>()).add(score);
        }

        Map<String, AggregatedMetric> aggregated = new LinkedHashMap<>();
        for (Map.Entry<String, List<MetricEvaluation>> entry : grouped.entrySet()) {
            String metricName = entry.getKey();
            List<MetricEvaluation> metricScores = entry.getValue();

            boolean allNumeric = metricScores.stream().allMatch(MetricEvaluation::isNumeric);
            if (allNumeric) {
                double mean = metricScores.stream().mapToDouble(MetricEvaluation::numericValue).average().orElse(0.0);
                aggregated.put(metricName, new AggregatedMetric(metricName, mean));
                continue;
            }

            Map<String, Integer> frequencies = new LinkedHashMap<>();
            for (MetricEvaluation metricScore : metricScores) {
                String key = String.valueOf(metricScore.value());
                frequencies.put(key, frequencies.getOrDefault(key, 0) + 1);
            }
            aggregated.put(metricName, new AggregatedMetric(metricName, frequencies));
        }

        return aggregated;
    }
}
