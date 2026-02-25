package io.ragas.api.dto;

import java.util.List;

public record R2rTransformRequest(
    List<String> userInputs,
    List<String> responses,
    List<List<String>> retrievedContexts,
    List<List<String>> referenceContexts,
    List<String> references
) {
}
