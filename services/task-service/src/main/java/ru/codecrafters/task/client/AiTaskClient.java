package ru.codecrafters.task.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.codecrafters.task.web.dto.AiGenerateTaskRequest;
import ru.codecrafters.task.web.dto.AiGenerateTaskResponse;

@Component
public class AiTaskClient {
    private final RestClient restClient;

    public AiTaskClient(RestClient.Builder restClientBuilder, @Value("${app.ai-service-url}") String aiServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(aiServiceUrl).build();
    }

    public AiGenerateTaskResponse generateTask(AiGenerateTaskRequest request) {
        return restClient.post()
                .uri("/api/ai/generate-task")
                .body(request)
                .retrieve()
                .body(AiGenerateTaskResponse.class);
    }
}
