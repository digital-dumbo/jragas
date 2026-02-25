package io.ragas.core.llm;

import java.util.List;

public record LlmRequest(
    String model,
    String prompt,
    Integer n,
    Double temperature,
    List<String> stop
) {

    public LlmRequest {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }
        stop = stop == null ? List.of() : List.copyOf(stop);
    }
}
