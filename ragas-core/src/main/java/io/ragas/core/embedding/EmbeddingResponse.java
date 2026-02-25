package io.ragas.core.embedding;

import io.ragas.core.llm.TokenUsage;
import java.util.List;

public record EmbeddingResponse(
    String model,
    List<List<Double>> embeddings,
    TokenUsage tokenUsage
) {

    public EmbeddingResponse {
        embeddings = embeddings == null ? List.of() : List.copyOf(embeddings);
    }
}
