package ru.codecrafters.ai.service;

import ru.codecrafters.ai.web.dto.GenerateTaskRequest;
import ru.codecrafters.ai.web.dto.GeneratedTaskResponse;

public interface TaskGenerationProvider {
    GeneratedTaskResponse generate(GenerateTaskRequest request);
}
