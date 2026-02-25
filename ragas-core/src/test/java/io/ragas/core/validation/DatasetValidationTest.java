package io.ragas.core.validation;

import io.ragas.core.metric.Metric;
import io.ragas.core.metric.MetricType;
import io.ragas.core.metric.MultiTurnMetric;
import io.ragas.core.metric.SingleTurnMetric;
import io.ragas.domain.dataset.EvaluationDataset;
import io.ragas.domain.dataset.MultiTurnSample;
import io.ragas.domain.dataset.SingleTurnSample;
import io.ragas.domain.message.HumanMessage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetValidationTest {

    @Test
    void validateRequiredColumnsFailsWhenMissingColumns() {
        Metric metric = new SingleTurnOnlyMetric("mock_metric", Map.of(MetricType.SINGLE_TURN, Set.of("user_input", "response")));
        EvaluationDataset dataset = new EvaluationDataset(
            List.of(new SingleTurnSample("What is X", null, null, null, null, null, null, null, null, null, null, null))
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> DatasetValidation.validateRequiredColumns(dataset, List.of(metric))
        );

        assertTrue(ex.getMessage().contains("requires the following additional columns"));
        assertTrue(ex.getMessage().contains("response"));
    }

    @Test
    void validateSupportedMetricsRejectsSingleTurnMetricForMultiTurnDataset() {
        Metric metric = new SingleTurnOnlyMetric("mock_metric", Map.of(MetricType.SINGLE_TURN, Set.of("user_input")));
        EvaluationDataset dataset = new EvaluationDataset(
            List.of(new MultiTurnSample(List.of(new HumanMessage("What is X", null)), null, null, null, null))
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> DatasetValidation.validateSupportedMetrics(dataset, List.of(metric))
        );

        assertTrue(ex.getMessage().contains("does not support the sample type"));
    }

    @Test
    void remapColumnNamesRenamesMappedColumnsOnly() {
        List<Map<String, Object>> rows = List.of(
            Map.of(
                "query", "",
                "rag_answer", "",
                "rag_contexts", List.of(""),
                "another_column", ""
            )
        );

        Map<String, String> columnMap = Map.of(
            "question", "query",
            "answer", "rag_answer"
        );

        List<Map<String, Object>> remapped = DatasetValidation.remapColumnNames(rows, columnMap);

        assertTrue(remapped.get(0).containsKey("question"));
        assertTrue(remapped.get(0).containsKey("answer"));
        assertTrue(remapped.get(0).containsKey("rag_contexts"));
        assertTrue(remapped.get(0).containsKey("another_column"));
    }

    private record SingleTurnOnlyMetric(String name, Map<MetricType, Set<String>> requiredColumns)
        implements Metric, SingleTurnMetric {
    }

    private record MultiTurnOnlyMetric(String name, Map<MetricType, Set<String>> requiredColumns)
        implements Metric, MultiTurnMetric {
    }
}
