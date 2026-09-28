package com.store.taskmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class AiPriorityService {

    @Value("${claude.api.key:demo-mode}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public record PriorityResult(int score, String reasoning) {}

    public PriorityResult scorePriority(String title, String description, List<String> currentTasks) {
        if (apiKey.equals("demo-mode")) {
            return demoScore(title);
        }

        try {
            String prompt = buildPrompt(title, description, currentTasks);

            Map<String, Object> body = Map.of(
                "model", "claude-haiku-4-5-20251001",
                "max_tokens", 300,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(response.body());
            String content = json.path("content").get(0).path("text").asText();

            JsonNode result = mapper.readTree(content);
            int score = result.path("score").asInt(50);
            String reasoning = result.path("reasoning").asText("AI prioritization applied");
            return new PriorityResult(Math.min(100, Math.max(1, score)), reasoning);

        } catch (Exception e) {
            return demoScore(title);
        }
    }

    private String buildPrompt(String title, String description, List<String> currentTasks) {
        return """
            You are a retail store task prioritization AI.

            A new task has arrived for a store associate:
            Title: %s
            Description: %s

            Current active tasks: %s

            Score this new task urgency from 1-100 (100 = most urgent).
            Consider: safety hazards = 90-100, customer service = 50-70, planned work = 30-50.

            Reply ONLY with valid JSON in this exact format:
            {"score": <number>, "reasoning": "<one sentence explanation>"}
            """.formatted(title, description, String.join(", ", currentTasks));
    }

    private PriorityResult demoScore(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("spill") || lower.contains("safety") || lower.contains("hazard")) {
            return new PriorityResult(95, "Safety hazard detected — immediate action required to prevent injury");
        } else if (lower.contains("bopis") || lower.contains("pickup") || lower.contains("order")) {
            return new PriorityResult(75, "Customer order with time commitment — must be fulfilled promptly");
        } else if (lower.contains("customer") || lower.contains("assist") || lower.contains("help")) {
            return new PriorityResult(60, "Active customer needs assistance — high service impact");
        } else {
            return new PriorityResult(40, "Planned operational task — can be scheduled around urgent items");
        }
    }
}
