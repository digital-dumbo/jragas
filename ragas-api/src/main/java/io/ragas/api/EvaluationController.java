package io.ragas.api;

import io.ragas.api.dto.AsyncRunCreatedResponse;
import io.ragas.api.dto.EvaluateRequest;
import io.ragas.api.dto.EvaluateResponse;
import io.ragas.api.dto.R2rTransformRequest;
import io.ragas.api.dto.R2rTransformResponse;
import io.ragas.api.dto.RunResponse;
import io.ragas.api.service.EvaluationApiService;
import io.ragas.integrations.transform.R2rDatasetTransformer;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EvaluationController {

    private final EvaluationApiService evaluationApiService;
    private final R2rDatasetTransformer r2rDatasetTransformer;

    public EvaluationController(
        EvaluationApiService evaluationApiService,
        R2rDatasetTransformer r2rDatasetTransformer
    ) {
        this.evaluationApiService = evaluationApiService;
        this.r2rDatasetTransformer = r2rDatasetTransformer;
    }

    @PostMapping("/evaluate")
    public EvaluateResponse evaluateSync(@Valid @RequestBody EvaluateRequest request) {
        return evaluationApiService.evaluateSync(request);
    }

    @PostMapping("/evaluate/async")
    public ResponseEntity<AsyncRunCreatedResponse> evaluateAsync(
        @Valid @RequestBody EvaluateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(evaluationApiService.evaluateAsync(request));
    }

    @GetMapping("/runs/{runId}")
    public RunResponse runStatus(@PathVariable("runId") UUID runId) {
        return evaluationApiService.getRun(runId);
    }

    @DeleteMapping("/runs/{runId}")
    public ResponseEntity<Void> cancelRun(@PathVariable("runId") UUID runId) {
        boolean cancelled = evaluationApiService.cancel(runId);
        return cancelled ? ResponseEntity.accepted().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/integrations/r2r/transform")
    public R2rTransformResponse transformR2r(@RequestBody R2rTransformRequest request) {
        return new R2rTransformResponse(
            r2rDatasetTransformer
                .transform(
                    request.userInputs(),
                    request.responses(),
                    request.retrievedContexts(),
                    request.referenceContexts(),
                    request.references()
                )
                .toRows()
        );
    }
}
