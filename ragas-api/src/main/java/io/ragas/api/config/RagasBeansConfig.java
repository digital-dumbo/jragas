package io.ragas.api.config;

import io.ragas.core.evaluation.EvaluationOrchestrator;
import io.ragas.integrations.tracing.LoggingRunTraceSink;
import io.ragas.integrations.tracing.RunTraceSink;
import io.ragas.integrations.transform.R2rDatasetTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagasBeansConfig {

    @Bean
    public EvaluationOrchestrator evaluationOrchestrator() {
        return new EvaluationOrchestrator();
    }

    @Bean
    public RunTraceSink runTraceSink() {
        return new LoggingRunTraceSink();
    }

    @Bean
    public R2rDatasetTransformer r2rDatasetTransformer() {
        return new R2rDatasetTransformer();
    }
}
