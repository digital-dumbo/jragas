package io.ragas.core.llm;

public enum FinishReason {
    STOP,
    LENGTH,
    TOOL_CALL,
    CONTENT_FILTER,
    UNKNOWN;

    public static FinishReason fromProviderValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "stop", "end_turn", "eos_token" -> STOP;
            case "length", "max_tokens" -> LENGTH;
            case "tool_calls", "function_call" -> TOOL_CALL;
            case "content_filter" -> CONTENT_FILTER;
            default -> UNKNOWN;
        };
    }
}
