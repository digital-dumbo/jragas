package io.ragas.integrations.transform;

import io.ragas.domain.dataset.EvaluationDataset;
import io.ragas.domain.dataset.SingleTurnSample;
import java.util.ArrayList;
import java.util.List;

public final class R2rDatasetTransformer {

    public EvaluationDataset transform(
        List<String> userInputs,
        List<String> responses,
        List<List<String>> retrievedContexts,
        List<List<String>> referenceContexts,
        List<String> references
    ) {
        int size = maxSize(userInputs, responses, retrievedContexts, referenceContexts, references);
        validateLength(size, userInputs, "userInputs");
        validateLength(size, responses, "responses");
        validateLength(size, retrievedContexts, "retrievedContexts");
        validateLength(size, referenceContexts, "referenceContexts");
        validateLength(size, references, "references");

        List<SingleTurnSample> samples = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            samples.add(new SingleTurnSample(
                itemOrNull(userInputs, i),
                itemOrNull(retrievedContexts, i),
                itemOrNull(referenceContexts, i),
                null,
                null,
                itemOrNull(responses, i),
                null,
                itemOrNull(references, i),
                null,
                null,
                null,
                null
            ));
        }

        return new EvaluationDataset(samples);
    }

    private static int maxSize(List<?>... lists) {
        int max = 0;
        for (List<?> list : lists) {
            if (list != null) {
                max = Math.max(max, list.size());
            }
        }
        return max;
    }

    private static void validateLength(int expected, List<?> list, String fieldName) {
        if (list != null && !list.isEmpty() && list.size() != expected) {
            throw new IllegalArgumentException(
                "Inconsistent length for " + fieldName + ": expected " + expected + ", got " + list.size()
            );
        }
    }

    private static <T> T itemOrNull(List<T> list, int index) {
        if (list == null || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }
}
