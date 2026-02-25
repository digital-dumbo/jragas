package io.ragas.adapters.openai;

import io.ragas.core.llm.LlmClientException;

public final class OpenAiHttpException extends LlmClientException {

    private final int statusCode;

    public OpenAiHttpException(int statusCode, String responseBody) {
        super("OpenAI request failed with status " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
