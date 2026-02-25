package io.ragas.domain.dataset;

import io.ragas.domain.message.AIMessage;
import io.ragas.domain.message.HumanMessage;
import io.ragas.domain.message.RagasMessage;
import io.ragas.domain.message.ToolCall;
import io.ragas.domain.message.ToolMessage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MultiTurnSample(
    List<RagasMessage> userInput,
    String reference,
    List<ToolCall> referenceToolCalls,
    Map<String, String> rubrics,
    List<String> referenceTopics
) implements EvaluationSample {

    public MultiTurnSample {
        if (userInput == null) {
            throw new IllegalArgumentException("userInput must not be null");
        }
        userInput = List.copyOf(userInput);
        referenceToolCalls = referenceToolCalls == null ? null : List.copyOf(referenceToolCalls);
        rubrics = rubrics == null ? null : Map.copyOf(rubrics);
        referenceTopics = referenceTopics == null ? null : List.copyOf(referenceTopics);
        validateUserInput(userInput);
    }

    @Override
    public SampleType sampleType() {
        return SampleType.MULTI_TURN;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("user_input", userInput.stream().map(RagasMessage::toMap).toList());
        putIfNotNull(map, "reference", reference);
        putIfNotNull(map, "reference_tool_calls", referenceToolCalls);
        putIfNotNull(map, "rubrics", rubrics);
        putIfNotNull(map, "reference_topics", referenceTopics);
        return map;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public static void validateUserInput(List<RagasMessage> messages) {
        boolean hasSeenAiMessage = false;

        for (int i = 0; i < messages.size(); i++) {
            RagasMessage message = messages.get(i);
            if (message instanceof AIMessage) {
                hasSeenAiMessage = true;
            } else if (message instanceof ToolMessage toolMessage) {
                if (!hasSeenAiMessage) {
                    throw new IllegalArgumentException(
                        "ToolMessage must be preceded by an AIMessage somewhere in the conversation."
                    );
                }

                if (i > 0) {
                    RagasMessage previous = messages.get(i - 1);
                    if (previous instanceof AIMessage aiMessage) {
                        if (aiMessage.toolCalls() == null || aiMessage.toolCalls().isEmpty()) {
                            throw new IllegalArgumentException(
                                "ToolMessage must follow an AIMessage where tools were called."
                            );
                        }
                    } else if (!(previous instanceof ToolMessage)) {
                        throw new IllegalArgumentException(
                            "ToolMessage must follow an AIMessage or another ToolMessage."
                        );
                    }
                }
            } else if (!(message instanceof HumanMessage)) {
                throw new IllegalArgumentException(
                    "All inputs must be instances of HumanMessage, AIMessage, or ToolMessage."
                );
            }
        }
    }

    public static MultiTurnSample fromRow(Map<String, Object> row) {
        return new MultiTurnSample(
            parseMessages(row.get("user_input")),
            asString(row.get("reference")),
            parseToolCalls(row.get("reference_tool_calls")),
            asStringMap(row.get("rubrics")),
            asStringList(row.get("reference_topics"))
        );
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Map<String, String> asStringMap(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected map value but got: " + value.getClass().getName());
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        return result;
    }

    private static List<String> asStringList(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected list value but got: " + value.getClass().getName());
        }

        List<String> result = new ArrayList<>();
        for (Object item : list) {
            result.add(item == null ? null : String.valueOf(item));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<RagasMessage> parseMessages(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("user_input is required for multi-turn samples");
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected user_input to be a list");
        }

        List<RagasMessage> messages = new ArrayList<>();
        for (Object raw : list) {
            if (raw instanceof RagasMessage message) {
                messages.add(message);
                continue;
            }
            if (!(raw instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Unsupported message payload type: " + raw.getClass().getName());
            }

            String type = map.get("type") == null ? "human" : String.valueOf(map.get("type"));
            String content = map.get("content") == null ? "" : String.valueOf(map.get("content"));

            if ("human".equals(type)) {
                messages.add(new HumanMessage(content, null));
            } else if ("tool".equals(type)) {
                messages.add(new ToolMessage(content, null));
            } else if ("ai".equals(type)) {
                List<ToolCall> toolCalls = parseToolCalls(map.get("tool_calls"));
                messages.add(new AIMessage(content, toolCalls, null));
            } else {
                throw new IllegalArgumentException("Unsupported message type: " + type);
            }
        }
        return messages;
    }

    private static List<ToolCall> parseToolCalls(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected tool call list");
        }

        List<ToolCall> toolCalls = new ArrayList<>();
        for (Object raw : list) {
            if (raw instanceof ToolCall toolCall) {
                toolCalls.add(toolCall);
                continue;
            }
            if (!(raw instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Unsupported tool call payload type: " + raw.getClass().getName());
            }
            Object name = map.get("name");
            Object args = map.get("args");
            if (name == null) {
                throw new IllegalArgumentException("Tool call name is required");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> castArgs = args instanceof Map<?, ?> m ? (Map<String, Object>) m : new LinkedHashMap<>();
            toolCalls.add(new ToolCall(String.valueOf(name), castArgs));
        }
        return toolCalls;
    }
}
