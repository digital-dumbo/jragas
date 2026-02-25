package io.ragas.adapters.openai;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public interface OpenAiTransport {

    OpenAiHttpResponse postJson(
        String url,
        Map<String, String> headers,
        String requestBody,
        Duration timeout
    ) throws IOException, InterruptedException;
}
