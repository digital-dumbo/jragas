package io.ragas.core.llm;

import java.util.Map;

public record LlmResponse(
    String text,
    FinishReason finishReason,
    TokenUsage tokenUsage,
    Map<String, Object> metadata
) {

    public LlmResponse {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
