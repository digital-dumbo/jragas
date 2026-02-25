package io.ragas.domain.dataset;

import io.ragas.domain.message.AIMessage;
import io.ragas.domain.message.HumanMessage;
import io.ragas.domain.message.ToolCall;
import io.ragas.domain.message.ToolMessage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationDatasetTest {

    @Test
    void rejectsMixedSampleTypes() {
        SingleTurnSample single = new SingleTurnSample(
            "What is X", null, null, null, null, "Y", null, null, null, null, null, null
        );
        MultiTurnSample multi = new MultiTurnSample(
            List.of(new HumanMessage("Hi", null)), null, null, null, null
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new EvaluationDataset(List.of(single, multi))
        );

        assertTrue(ex.getMessage().contains("Sample at index 1 is of type"));
    }

    @Test
    void rejectsToolMessageWithoutPriorAiMessage() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new MultiTurnSample(
                List.of(new HumanMessage("Question", null), new ToolMessage("Result", null)),
                null,
                null,
                null,
                null
            )
        );

        assertEquals(
            "ToolMessage must be preceded by an AIMessage somewhere in the conversation.",
            ex.getMessage()
        );
    }

    @Test
    void rejectsToolMessageAfterAiWithoutToolCalls() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new MultiTurnSample(
                List.of(
                    new HumanMessage("Question", null),
                    new AIMessage("Thinking", null, null),
                    new ToolMessage("Tool output", null)
                ),
                null,
                null,
                null,
                null
            )
        );

        assertEquals("ToolMessage must follow an AIMessage where tools were called.", ex.getMessage());
    }

    @Test
    void acceptsValidMultiTurnMessageFlow() {
        MultiTurnSample sample = new MultiTurnSample(
            List.of(
                new HumanMessage("Question", null),
                new AIMessage("Calling tool", List.of(new ToolCall("search", Map.of("q", "x"))), null),
                new ToolMessage("Result", null),
                new AIMessage("Final answer", null, null)
            ),
            "ref",
            null,
            null,
            null
        );

        assertEquals(4, sample.userInput().size());
    }
}
