package ru.codecrafters.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.codecrafters.ai.web.dto.GenerateTaskRequest;
import ru.codecrafters.ai.web.dto.GeneratedTaskResponse;

@Service
public class TaskGenerationService {
    private static final Logger log = LoggerFactory.getLogger(TaskGenerationService.class);

    private final String mode;
    private final boolean mockEnabled;
    private final MockTaskGenerationProvider mockProvider;
    private final GigachatTaskGenerationProvider gigachatProvider;

    public TaskGenerationService(
            @Value("${app.ai.mode}") String mode,
            @Value("${app.ai.mock-enabled}") boolean mockEnabled,
            MockTaskGenerationProvider mockProvider,
            GigachatTaskGenerationProvider gigachatProvider
    ) {
        this.mode = mode;
        this.mockEnabled = mockEnabled;
        this.mockProvider = mockProvider;
        this.gigachatProvider = gigachatProvider;
    }

    public GeneratedTaskResponse generate(GenerateTaskRequest request) {
        String normalizedMode = mode == null ? "mock" : mode.trim().toLowerCase();
        log.info(
                "Generating task: mode={}, mockEnabled={}, topic={}, difficulty={}, direction={}",
                normalizedMode,
                mockEnabled,
                request.topic(),
                request.difficulty(),
                request.direction()
        );

        if ("gigachat".equals(normalizedMode)) {
            try {
                return gigachatProvider.generate(request);
            } catch (RuntimeException exception) {
                if (mockEnabled) {
                    log.warn("GigaChat generation failed, using mock fallback: {}", exception.getMessage());
                    return mockProvider.generate(request);
                }
                throw exception;
            }
        }

        if (!"mock".equals(normalizedMode)) {
            log.warn("Unknown AI_MODE={}, using mock generation", normalizedMode);
        }

        return mockProvider.generate(request);
    }
}
