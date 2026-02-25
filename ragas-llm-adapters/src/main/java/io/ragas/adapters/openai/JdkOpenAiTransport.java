package io.ragas.adapters.openai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class JdkOpenAiTransport implements OpenAiTransport {

    private final HttpClient httpClient;

    public JdkOpenAiTransport(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public OpenAiHttpResponse postJson(
        String url,
        Map<String, String> headers,
        String requestBody,
        Duration timeout
    ) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(timeout)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        HttpResponse<String> response = httpClient.send(
            builder.build(),
            HttpResponse.BodyHandlers.ofString()
        );

        return new OpenAiHttpResponse(response.statusCode(), response.body());
    }
}
