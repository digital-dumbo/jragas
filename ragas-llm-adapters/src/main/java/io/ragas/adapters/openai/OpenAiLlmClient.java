package io.ragas.adapters.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ragas.core.llm.FinishReason;
import io.ragas.core.llm.LlmClient;
import io.ragas.core.llm.LlmClientException;
import io.ragas.core.llm.LlmRequest;
import io.ragas.core.llm.LlmResponse;
import io.ragas.core.llm.TokenUsage;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OpenAiLlmClient implements LlmClient {

    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(429, 500, 502, 503, 504);

    private final ObjectMapper objectMapper;
    private final OpenAiTransport transport;
    private final OpenAiConfig config;

    public OpenAiLlmClient(ObjectMapper objectMapper, OpenAiTransport transport, OpenAiConfig config) {
        this.objectMapper = objectMapper;
        this.transport = transport;
        this.config = config;
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String model = (request.model() == null || request.model().isBlank())
            ? config.defaultModel()
            : request.model();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", List.of(Map.of("role", "user", "content", request.prompt())));

        if (request.n() != null) {
            payload.put("n", request.n());
        }
        if (request.temperature() != null) {
            payload.put("temperature", request.temperature());
        }
        if (!request.stop().isEmpty()) {
            payload.put("stop", request.stop());
        }

        String requestBody = toJson(payload);
        OpenAiHttpResponse response = executeWithRetry(requestBody, config.timeout());

        if (response.statusCode() >= 400) {
            throw new OpenAiHttpException(response.statusCode(), response.body());
        }

        return parseResponse(response.body(), model);
    }

    private OpenAiHttpResponse executeWithRetry(String requestBody, Duration timeout) {
        int maxAttempts = config.maxRetries() + 1;
        long sleepMillis = 250L;
        Throwable lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                OpenAiHttpResponse response = transport.postJson(
                    config.baseUrl() + "/v1/chat/completions",
                    Map.of("Authorization", "Bearer " + config.apiKey()),
                    requestBody,
                    timeout
                );

                if (response.statusCode() >= 400 && RETRYABLE_STATUS_CODES.contains(response.statusCode()) && attempt < maxAttempts) {
                    sleep(sleepMillis);
                    sleepMillis = Math.min(2000L, sleepMillis * 2L);
                    continue;
                }

                return response;
            } catch (IOException e) {
                lastError = e;
                if (attempt >= maxAttempts) {
                    break;
                }
                sleep(sleepMillis);
                sleepMillis = Math.min(2000L, sleepMillis * 2L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LlmClientException("OpenAI request interrupted", e);
            }
        }

        throw new LlmClientException("OpenAI request failed after retries", lastError);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmClientException("Retry sleep interrupted", e);
        }
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new LlmClientException("Failed to serialize OpenAI request", e);
        }
    }

    private LlmResponse parseResponse(String body, String fallbackModel) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new LlmClientException("OpenAI response contained no choices");
            }

            JsonNode firstChoice = choices.get(0);
            String text = firstChoice.path("message").path("content").asText("");
            String finish = firstChoice.path("finish_reason").isMissingNode()
                ? null
                : firstChoice.path("finish_reason").asText();

            String model = root.path("model").asText(fallbackModel);
            JsonNode usage = root.path("usage");

            Integer promptTokens = asNullableInt(usage.get("prompt_tokens"));
            Integer completionTokens = asNullableInt(usage.get("completion_tokens"));
            Integer totalTokens = asNullableInt(usage.get("total_tokens"));

            TokenUsage tokenUsage = new TokenUsage(promptTokens, completionTokens, totalTokens, model);

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("provider", "openai");
            metadata.put("model", model);
            metadata.put("choice_count", choices.size());

            return new LlmResponse(text, FinishReason.fromProviderValue(finish), tokenUsage, metadata);
        } catch (IOException e) {
            throw new LlmClientException("Failed to parse OpenAI response", e);
        }
    }

    private static Integer asNullableInt(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.asInt();
    }
}
