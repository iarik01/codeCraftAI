package ru.codecrafters.task.web.dto;

import java.util.Map;

public record UpdateTaskContentRequest(Map<String, Object> generatedContent) {
}
