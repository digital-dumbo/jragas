package io.ragas.adapters.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ragas.core.llm.FinishReason;
import io.ragas.core.llm.LlmClientException;
import io.ragas.core.llm.LlmRequest;
import io.ragas.core.llm.LlmResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiLlmClientTest {

    @Test
    void normalizesFinishReasonAndUsage() {
        FakeTransport transport = new FakeTransport();
        transport.enqueue(new OpenAiHttpResponse(200, """
            {
              "id": "chatcmpl-1",
              "model": "gpt-4o-mini",
              "choices": [
                {
                  "index": 0,
                  "message": {"role": "assistant", "content": "hello"},
                  "finish_reason": "stop"
                }
              ],
              "usage": {
                "prompt_tokens": 10,
                "completion_tokens": 4,
                "total_tokens": 14
              }
            }
            """));

        OpenAiLlmClient client = new OpenAiLlmClient(
            new ObjectMapper(),
            transport,
            new OpenAiConfig("test-key", "https://api.openai.com", "gpt-4o-mini", Duration.ofSeconds(2), 1)
        );

        LlmResponse response = client.generate(new LlmRequest(null, "hi", 1, 0.1, null));

        assertEquals("hello", response.text());
        assertEquals(FinishReason.STOP, response.finishReason());
        assertEquals(10, response.tokenUsage().inputTokens());
        assertEquals(4, response.tokenUsage().outputTokens());
        assertEquals(14, response.tokenUsage().totalTokens());
        assertEquals("gpt-4o-mini", response.tokenUsage().model());
    }

    @Test
    void retriesOn429AndThenSucceeds() {
        FakeTransport transport = new FakeTransport();
        transport.enqueue(new OpenAiHttpResponse(429, "{\"error\":\"rate limited\"}"));
        transport.enqueue(new OpenAiHttpResponse(200, """
            {
              "model": "gpt-4o-mini",
              "choices": [{"message": {"content": "ok"}, "finish_reason": "length"}],
              "usage": {"prompt_tokens": 1, "completion_tokens": 1, "total_tokens": 2}
            }
            """));

        OpenAiLlmClient client = new OpenAiLlmClient(
            new ObjectMapper(),
            transport,
            new OpenAiConfig("test-key", "https://api.openai.com", "gpt-4o-mini", Duration.ofSeconds(2), 2)
        );

        LlmResponse response = client.generate(new LlmRequest("gpt-4o-mini", "hi", 1, null, null));

        assertEquals("ok", response.text());
        assertEquals(FinishReason.LENGTH, response.finishReason());
        assertEquals(2, transport.calls());
    }

    @Test
    void failsAfterMaxRetriesOnIoErrors() {
        FakeTransport transport = new FakeTransport();
        transport.enqueue(new IOException("network down"));
        transport.enqueue(new IOException("still down"));

        OpenAiLlmClient client = new OpenAiLlmClient(
            new ObjectMapper(),
            transport,
            new OpenAiConfig("test-key", "https://api.openai.com", "gpt-4o-mini", Duration.ofSeconds(2), 1)
        );

        assertThrows(
            LlmClientException.class,
            () -> client.generate(new LlmRequest("gpt-4o-mini", "hi", 1, null, null))
        );
        assertEquals(2, transport.calls());
    }

    @Test
    void throwsOnPermanentHttpError() {
        FakeTransport transport = new FakeTransport();
        transport.enqueue(new OpenAiHttpResponse(400, "{\"error\":\"bad request\"}"));

        OpenAiLlmClient client = new OpenAiLlmClient(
            new ObjectMapper(),
            transport,
            new OpenAiConfig("test-key", "https://api.openai.com", "gpt-4o-mini", Duration.ofSeconds(2), 2)
        );

        assertThrows(
            OpenAiHttpException.class,
            () -> client.generate(new LlmRequest("gpt-4o-mini", "hi", 1, null, null))
        );
        assertEquals(1, transport.calls());
    }

    private static final class FakeTransport implements OpenAiTransport {
        private final Deque<Object> responses = new ArrayDeque<>();
        private int calls;

        void enqueue(OpenAiHttpResponse response) {
            responses.addLast(response);
        }

        void enqueue(IOException exception) {
            responses.addLast(exception);
        }

        int calls() {
            return calls;
        }

        @Override
        public OpenAiHttpResponse postJson(String url, Map<String, String> headers, String requestBody, Duration timeout)
            throws IOException {
            calls++;
            Object next = responses.pollFirst();
            if (next == null) {
                throw new IOException("No prepared response");
            }
            if (next instanceof IOException ioException) {
                throw ioException;
            }
            return (OpenAiHttpResponse) next;
        }
    }
}
