package io.ragas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "ragas-cli",
    mixinStandardHelpOptions = true,
    subcommands = {
        RagasCli.EvaluateSyncCommand.class,
        RagasCli.EvaluateAsyncCommand.class,
        RagasCli.RunStatusCommand.class
    }
)
public class RagasCli implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("Use a subcommand. Try --help for options.");
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RagasCli()).execute(args);
        System.exit(exitCode);
    }

    @Command(name = "evaluate-sync", description = "Run synchronous evaluation via API")
    static class EvaluateSyncCommand implements Callable<Integer> {

        @Option(names = {"--url"}, defaultValue = "http://localhost:8080/api/v1/evaluate")
        String url;

        @Option(names = {"--request-file"}, required = true)
        Path requestFile;

        @Override
        public Integer call() throws Exception {
            String payload = Files.readString(requestFile);
            String response = post(url, payload);
            prettyPrintJson(response);
            return 0;
        }
    }

    @Command(name = "evaluate-async", description = "Submit asynchronous evaluation run")
    static class EvaluateAsyncCommand implements Callable<Integer> {

        @Option(names = {"--url"}, defaultValue = "http://localhost:8080/api/v1/evaluate/async")
        String url;

        @Option(names = {"--request-file"}, required = true)
        Path requestFile;

        @Override
        public Integer call() throws Exception {
            String payload = Files.readString(requestFile);
            String response = post(url, payload);
            prettyPrintJson(response);
            return 0;
        }
    }

    @Command(name = "run-status", description = "Get run status by runId")
    static class RunStatusCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Run ID")
        String runId;

        @Option(names = {"--base-url"}, defaultValue = "http://localhost:8080")
        String baseUrl;

        @Override
        public Integer call() throws Exception {
            String url = baseUrl + "/api/v1/runs/" + runId;
            String response = get(url);
            prettyPrintJson(response);
            return 0;
        }
    }

    private static String post(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("Request failed: HTTP " + response.statusCode() + " body=" + response.body());
        }
        return response.body();
    }

    private static String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("Request failed: HTTP " + response.statusCode() + " body=" + response.body());
        }
        return response.body();
    }

    private static void prettyPrintJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Object parsed = objectMapper.readValue(json, Object.class);
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed));
    }
}
