package io.ragas.core.llm;

public record TokenUsage(
    Integer inputTokens,
    Integer outputTokens,
    Integer totalTokens,
    String model
) {
}
