package io.ragas.domain.dataset;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SingleTurnSample(
    String userInput,
    List<String> retrievedContexts,
    List<String> referenceContexts,
    List<Object> retrievedContextIds,
    List<Object> referenceContextIds,
    String response,
    List<String> multiResponses,
    String reference,
    Map<String, String> rubrics,
    String personaName,
    String queryStyle,
    String queryLength
) implements EvaluationSample {

    public SingleTurnSample {
        retrievedContexts = copyList(retrievedContexts);
        referenceContexts = copyList(referenceContexts);
        retrievedContextIds = copyList(retrievedContextIds);
        referenceContextIds = copyList(referenceContextIds);
        multiResponses = copyList(multiResponses);
        rubrics = rubrics == null ? null : Map.copyOf(rubrics);
    }

    private static <T> List<T> copyList(List<T> values) {
        return values == null ? null : List.copyOf(values);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        putIfNotNull(map, "user_input", userInput);
        putIfNotNull(map, "retrieved_contexts", retrievedContexts);
        putIfNotNull(map, "reference_contexts", referenceContexts);
        putIfNotNull(map, "retrieved_context_ids", retrievedContextIds);
        putIfNotNull(map, "reference_context_ids", referenceContextIds);
        putIfNotNull(map, "response", response);
        putIfNotNull(map, "multi_responses", multiResponses);
        putIfNotNull(map, "reference", reference);
        putIfNotNull(map, "rubrics", rubrics);
        putIfNotNull(map, "persona_name", personaName);
        putIfNotNull(map, "query_style", queryStyle);
        putIfNotNull(map, "query_length", queryLength);
        return map;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : toMap().entrySet()) {
            sb.append("\n").append(entry.getKey()).append(":\n\t").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public SampleType sampleType() {
        return SampleType.SINGLE_TURN;
    }

    public static SingleTurnSample fromRow(Map<String, Object> row) {
        return new SingleTurnSample(
            asString(row.get("user_input")),
            asStringList(row.get("retrieved_contexts")),
            asStringList(row.get("reference_contexts")),
            asObjectList(row.get("retrieved_context_ids")),
            asObjectList(row.get("reference_context_ids")),
            asString(row.get("response")),
            asStringList(row.get("multi_responses")),
            asString(row.get("reference")),
            asStringMap(row.get("rubrics")),
            asString(row.get("persona_name")),
            asString(row.get("query_style")),
            asString(row.get("query_length"))
        );
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected list value but got: " + value.getClass().getName());
        }
        List<String> converted = new ArrayList<>();
        for (Object item : list) {
            converted.add(item == null ? null : String.valueOf(item));
        }
        return converted;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asObjectList(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected list value but got: " + value.getClass().getName());
        }
        return (List<Object>) list;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> asStringMap(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected map value but got: " + value.getClass().getName());
        }
        Map<String, String> converted = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            converted.put(String.valueOf(entry.getKey()), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        return converted;
    }
}
