package ru.codecrafters.ai.web.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;

public record GeneratedTaskResponse(
        String title,
        String taskType,
        String description,
        String instructions,
        String inputData,
        String expectedResult,
        List<String> hints,
        String difficulty,
        String topic,
        String aiProvider,
        @JsonIgnore Map<String, Object> additionalFields
) {
    public GeneratedTaskResponse(
            String title,
            String taskType,
            String description,
            String instructions,
            String inputData,
            String expectedResult,
            List<String> hints,
            String difficulty,
            String topic,
            String aiProvider
    ) {
        this(title, taskType, description, instructions, inputData, expectedResult, hints, difficulty, topic, aiProvider, Map.of());
    }

    @JsonAnyGetter
    public Map<String, Object> jsonAdditionalFields() {
        return additionalFields == null ? Map.of() : additionalFields;
    }
}
