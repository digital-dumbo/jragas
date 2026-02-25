package io.ragas.api.dto;

import java.util.List;
import java.util.Map;

public record R2rTransformResponse(
    List<Map<String, Object>> datasetRows
) {
}
