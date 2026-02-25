package io.ragas.domain.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record ToolMessage(String content, Map<String, Object> metadata) implements RagasMessage {

    public ToolMessage {
        Objects.requireNonNull(content, "content must not be null");
        metadata = metadata == null ? null : Map.copyOf(metadata);
    }

    @Override
    public String type() {
        return "tool";
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type());
        map.put("content", content);
        if (metadata != null) {
            map.put("metadata", metadata);
        }
        return map;
    }
}
