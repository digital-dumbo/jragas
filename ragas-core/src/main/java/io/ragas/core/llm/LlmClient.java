package io.ragas.core.llm;

public interface LlmClient {

    LlmResponse generate(LlmRequest request);
}
