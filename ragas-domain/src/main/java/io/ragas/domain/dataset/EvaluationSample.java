package io.ragas.domain.dataset;

import java.util.List;
import java.util.Map;

public interface EvaluationSample {

    Map<String, Object> toMap();

    default List<String> getFeatures() {
        return List.copyOf(toMap().keySet());
    }

    SampleType sampleType();
}
