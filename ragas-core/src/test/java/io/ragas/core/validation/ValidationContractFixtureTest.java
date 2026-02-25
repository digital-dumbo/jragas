package io.ragas.core.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ragas.core.metric.Metric;
import io.ragas.core.metric.MetricType;
import io.ragas.core.metric.SingleTurnMetric;
import io.ragas.domain.dataset.EvaluationDataset;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationContractFixtureTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void requiredColumnsContractFromFixture() {
        ValidationFixture fixture = readFixture(
            "contracts/validation/required-columns-single-turn.json"
        );

        EvaluationDataset dataset = EvaluationDataset.fromRows(fixture.datasetRows);
        Metric metric = toMetric(fixture.metric);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> DatasetValidation.validateRequiredColumns(dataset, List.of(metric))
        );

        for (String expectedFragment : fixture.expected.exceptionContains) {
            assertTrue(ex.getMessage().contains(expectedFragment));
        }
    }

    @Test
    void supportedMetricContractFromFixture() {
        ValidationFixture fixture = readFixture(
            "contracts/validation/unsupported-metric-multi-turn.json"
        );

        EvaluationDataset dataset = EvaluationDataset.fromRows(fixture.datasetRows);
        Metric metric = toMetric(fixture.metric);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> DatasetValidation.validateSupportedMetrics(dataset, List.of(metric))
        );

        for (String expectedFragment : fixture.expected.exceptionContains) {
            assertTrue(ex.getMessage().contains(expectedFragment));
        }
    }

    @Test
    void remapContractFromFixture() {
        RemapFixture fixture = readFixture(
            "contracts/validation/remap-columns.json",
            new TypeReference<>() {}
        );

        List<Map<String, Object>> remapped = DatasetValidation.remapColumnNames(
            fixture.rows,
            fixture.columnMap
        );

        for (String expectedColumn : fixture.expectedColumns) {
            assertTrue(remapped.get(0).containsKey(expectedColumn));
        }
    }

    private static Metric toMetric(MetricFixture metricFixture) {
        Map<MetricType, Set<String>> requiredColumns = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : metricFixture.requiredColumns.entrySet()) {
            requiredColumns.put(MetricType.valueOf(entry.getKey()), Set.copyOf(entry.getValue()));
        }

        if ("single_turn".equals(metricFixture.kind)) {
            return new SingleTurnOnlyMetric(metricFixture.name, requiredColumns);
        }

        throw new IllegalArgumentException("Unsupported metric fixture kind: " + metricFixture.kind);
    }

    private static ValidationFixture readFixture(String path) {
        return readFixture(path, new TypeReference<>() {});
    }

    private static <T> T readFixture(String path, TypeReference<T> typeReference) {
        try (InputStream stream = ValidationContractFixtureTest.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Fixture not found: " + path);
            }
            return OBJECT_MAPPER.readValue(stream, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read fixture: " + path, e);
        }
    }

    private record SingleTurnOnlyMetric(String name, Map<MetricType, Set<String>> requiredColumns)
        implements Metric, SingleTurnMetric {
    }

    private static final class ValidationFixture {
        public String name;
        public List<Map<String, Object>> datasetRows;
        public MetricFixture metric;
        public ExpectedExceptionFixture expected;
    }

    private static final class MetricFixture {
        public String name;
        public String kind;
        public Map<String, List<String>> requiredColumns;
    }

    private static final class ExpectedExceptionFixture {
        public List<String> exceptionContains;
    }

    private static final class RemapFixture {
        public String name;
        public List<Map<String, Object>> rows;
        public Map<String, String> columnMap;
        public List<String> expectedColumns;
    }
}
