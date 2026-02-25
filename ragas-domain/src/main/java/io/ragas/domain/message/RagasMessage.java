package io.ragas.domain.message;

import java.util.Map;

public interface RagasMessage {

    String type();

    String content();

    Map<String, Object> toMap();
}
