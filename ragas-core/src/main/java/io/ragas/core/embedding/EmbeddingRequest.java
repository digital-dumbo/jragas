package io.ragas.core.embedding;

import java.util.List;

public record EmbeddingRequest(
    String model,
    List<String> input
) {

    public EmbeddingRequest {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("input must not be empty");
        }
        input = List.copyOf(input);
    }
}
