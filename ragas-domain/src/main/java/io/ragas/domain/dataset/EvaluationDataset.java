package io.ragas.domain.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class EvaluationDataset implements Iterable<EvaluationSample> {

    private final List<EvaluationSample> samples;
    private final String backend;
    private final String name;

    public EvaluationDataset(List<? extends EvaluationSample> samples, String backend, String name) {
        this.samples = validateSampleTypes(samples);
        this.backend = backend;
        this.name = name;
    }

    public EvaluationDataset(List<? extends EvaluationSample> samples) {
        this(samples, null, null);
    }

    public static EvaluationDataset fromRows(List<Map<String, Object>> rows, String backend, String name) {
        if (rows == null || rows.isEmpty()) {
            return new EvaluationDataset(List.of(), backend, name);
        }

        boolean isMultiTurn = rows.stream().allMatch(row -> row.containsKey("user_input") && row.get("user_input") instanceof List<?>);
        List<EvaluationSample> samples = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            samples.add(isMultiTurn ? MultiTurnSample.fromRow(row) : SingleTurnSample.fromRow(row));
        }
        return new EvaluationDataset(samples, backend, name);
    }

    public static EvaluationDataset fromRows(List<Map<String, Object>> rows) {
        return fromRows(rows, null, null);
    }

    private static List<EvaluationSample> validateSampleTypes(List<? extends EvaluationSample> values) {
        if (values == null) {
            throw new IllegalArgumentException("samples must not be null");
        }
        if (values.isEmpty()) {
            return List.of();
        }

        Class<?> expectedType = values.get(0).getClass();
        List<EvaluationSample> copy = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            EvaluationSample sample = values.get(i);
            if (!expectedType.equals(sample.getClass())) {
                throw new IllegalArgumentException(
                    "Sample at index " + i + " is of type " + sample.getClass().getName()
                        + ", expected " + expectedType.getName()
                );
            }
            copy.add(sample);
        }
        return Collections.unmodifiableList(copy);
    }

    public List<Map<String, Object>> toRows() {
        return samples.stream().map(EvaluationSample::toMap).toList();
    }

    public SampleType getSampleType() {
        if (samples.isEmpty()) {
            throw new IllegalStateException("Cannot infer sample type from an empty dataset");
        }
        return samples.get(0).sampleType();
    }

    public boolean isMultiTurn() {
        return getSampleType() == SampleType.MULTI_TURN;
    }

    public List<String> features() {
        if (samples.isEmpty()) {
            return List.of();
        }
        return samples.get(0).getFeatures();
    }

    public int size() {
        return samples.size();
    }

    public EvaluationSample get(int index) {
        return samples.get(index);
    }

    public List<EvaluationSample> samples() {
        return samples;
    }

    public String backend() {
        return backend;
    }

    public String name() {
        return name;
    }

    @Override
    public Iterator<EvaluationSample> iterator() {
        return samples.iterator();
    }

    @Override
    public String toString() {
        return "EvaluationDataset(features=" + features() + ", len=" + samples.size() + ")";
    }
}
