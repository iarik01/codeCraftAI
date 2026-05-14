package ru.codecrafters.task.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.codecrafters.task.web.dto.AiGeneratedAssignmentResponse;
import ru.codecrafters.task.web.dto.CreateGenerationRequest;

@Component
public class AiGenerationClient {
    private final RestClient restClient;

    public AiGenerationClient(RestClient.Builder restClientBuilder, @Value("${app.ai-service-url}") String aiServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(aiServiceUrl).build();
    }

    public AiGeneratedAssignmentResponse generate(CreateGenerationRequest request) {
        return restClient.post()
                .uri("/api/ai/generate")
                .body(request)
                .retrieve()
                .body(AiGeneratedAssignmentResponse.class);
    }
}
