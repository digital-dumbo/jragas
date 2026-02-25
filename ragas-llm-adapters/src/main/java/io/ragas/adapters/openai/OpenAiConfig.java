package io.ragas.adapters.openai;

import java.time.Duration;

public record OpenAiConfig(
    String apiKey,
    String baseUrl,
    String defaultModel,
    Duration timeout,
    int maxRetries
) {

    public OpenAiConfig {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        baseUrl = (baseUrl == null || baseUrl.isBlank())
            ? "https://api.openai.com"
            : baseUrl;
        if (defaultModel == null || defaultModel.isBlank()) {
            throw new IllegalArgumentException("defaultModel must not be blank");
        }
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
    }
}
