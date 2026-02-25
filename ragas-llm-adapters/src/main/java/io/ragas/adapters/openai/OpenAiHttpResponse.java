package io.ragas.adapters.openai;

public record OpenAiHttpResponse(int statusCode, String body) {
}
