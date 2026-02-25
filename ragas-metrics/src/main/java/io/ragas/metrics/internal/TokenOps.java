package io.ragas.metrics.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class TokenOps {

    private TokenOps() {
    }

    public static Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        String[] raw = value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+");
        Set<String> tokens = new HashSet<>();
        Arrays.stream(raw)
            .filter(token -> !token.isBlank())
            .forEach(tokens::add);
        return tokens;
    }

    public static double overlapRatio(Set<String> left, Set<String> right) {
        if (left.isEmpty()) {
            return 0.0;
        }
        int overlap = 0;
        for (String token : left) {
            if (right.contains(token)) {
                overlap++;
            }
        }
        return ((double) overlap) / left.size();
    }

    public static double jaccard(Set<String> left, Set<String> right) {
        if (left.isEmpty() && right.isEmpty()) {
            return 0.0;
        }

        Set<String> union = new HashSet<>(left);
        union.addAll(right);
        if (union.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return ((double) intersection.size()) / union.size();
    }

    public static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
