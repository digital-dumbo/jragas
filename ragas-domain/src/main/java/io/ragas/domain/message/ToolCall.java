package io.ragas.domain.message;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record ToolCall(String name, Map<String, Object> args) {

    public ToolCall {
        Objects.requireNonNull(name, "name must not be null");
        args = args == null ? Collections.emptyMap() : Collections.unmodifiableMap(args);
    }
}
