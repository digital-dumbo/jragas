package io.ragas.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record EvaluateRequest(
    @NotEmpty List<Map<String, Object>> datasetRows,
    List<String> metrics,
    Integer batchSize,
    Long rowTimeoutMs,
    Integer maxWorkers
) {
}
