package io.ragas.domain.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record AIMessage(String content, List<ToolCall> toolCalls, Map<String, Object> metadata)
    implements RagasMessage {

    public AIMessage {
        Objects.requireNonNull(content, "content must not be null");
        toolCalls = toolCalls == null ? null : List.copyOf(toolCalls);
        metadata = metadata == null ? null : Map.copyOf(metadata);
    }

    @Override
    public String type() {
        return "ai";
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type());
        map.put("content", content);
        if (toolCalls != null) {
            map.put("tool_calls", toolCalls.stream().map(tc -> Map.of("name", tc.name(), "args", tc.args())).toList());
        }
        if (metadata != null) {
            map.put("metadata", metadata);
        }
        return map;
    }
}
