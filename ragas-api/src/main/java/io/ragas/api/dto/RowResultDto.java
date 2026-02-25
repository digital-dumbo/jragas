package io.ragas.api.dto;

import java.util.Map;

public record RowResultDto(
    int rowIndex,
    Map<String, Object> metrics,
    String error
) {
}
