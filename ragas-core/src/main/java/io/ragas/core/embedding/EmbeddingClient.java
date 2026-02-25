package io.ragas.core.embedding;

public interface EmbeddingClient {

    EmbeddingResponse embed(EmbeddingRequest request);
}
