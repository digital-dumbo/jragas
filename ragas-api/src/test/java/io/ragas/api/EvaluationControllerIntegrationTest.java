package io.ragas.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EvaluationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void evaluateSyncReturnsCompletedResult() throws Exception {
        Map<String, Object> request = Map.of(
            "datasetRows", List.of(
                Map.of(
                    "user_input", "What is the capital of France?",
                    "response", "Paris is the capital of France.",
                    "retrieved_contexts", List.of("Paris is the capital city of France."),
                    "reference_contexts", List.of("Paris is the capital of France.")
                )
            ),
            "metrics", List.of("answer_relevancy", "faithfulness")
        );

        mockMvc.perform(post("/api/v1/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Trace-Id"))
            .andExpect(jsonPath("$.runId", notNullValue()))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.rows[0].metrics.answer_relevancy", notNullValue()))
            .andExpect(jsonPath("$.rows[0].metrics.faithfulness", notNullValue()))
            .andExpect(jsonPath("$.aggregated.answer_relevancy", notNullValue()));
    }

    @Test
    void evaluateAsyncThenFetchRunStatus() throws Exception {
        Map<String, Object> request = Map.of(
            "datasetRows", List.of(
                Map.of(
                    "user_input", "Q1",
                    "response", "A1",
                    "retrieved_contexts", List.of("A1"),
                    "reference_contexts", List.of("A1")
                ),
                Map.of(
                    "user_input", "Q2",
                    "response", "A2",
                    "retrieved_contexts", List.of("A2"),
                    "reference_contexts", List.of("A2")
                )
            ),
            "metrics", List.of("answer_relevancy")
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/evaluate/async")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(header().exists("X-Trace-Id"))
            .andExpect(jsonPath("$.runId", notNullValue()))
            .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID runId = UUID.fromString(createJson.path("runId").asText());

        mockMvc.perform(get("/api/v1/runs/{runId}", runId))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Trace-Id"))
            .andExpect(jsonPath("$.runId").value(runId.toString()))
            .andExpect(jsonPath("$.status", oneOf("RUNNING", "COMPLETED", "CANCELLED", "TIMED_OUT", "FAILED")));
    }

    @Test
    void r2rTransformProducesDatasetRows() throws Exception {
        Map<String, Object> request = Map.of(
            "userInputs", List.of("What is X?", "What is Y?"),
            "responses", List.of("X is ...", "Y is ..."),
            "retrievedContexts", List.of(List.of("ctx-x"), List.of("ctx-y")),
            "referenceContexts", List.of(List.of("ref-x"), List.of("ref-y")),
            "references", List.of("ref answer x", "ref answer y")
        );

        mockMvc.perform(post("/api/v1/integrations/r2r/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datasetRows[0].user_input").value("What is X?"))
            .andExpect(jsonPath("$.datasetRows[1].response").value("Y is ..."));
    }

    @Test
    void validationErrorIncludesTraceId() throws Exception {
        Map<String, Object> invalidRequest = Map.of(
            "metrics", List.of("answer_relevancy")
        );

        MvcResult result = mockMvc.perform(post("/api/v1/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(header().exists("X-Trace-Id"))
            .andExpect(jsonPath("$.traceId", notNullValue()))
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String traceId = body.path("traceId").asText();
        String headerTraceId = result.getResponse().getHeader("X-Trace-Id");
        assertEquals(traceId, headerTraceId);
    }
}
