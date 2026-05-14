package ru.codecrafters.ai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.codecrafters.ai.model.AgeGroup;
import ru.codecrafters.ai.model.AssignmentType;
import ru.codecrafters.ai.model.Difficulty;
import ru.codecrafters.ai.model.Direction;

public record GenerateTaskRequest(
        @NotNull Direction direction,
        @NotBlank @Size(max = 255) String topic,
        @NotNull Difficulty difficulty,
        @NotNull AgeGroup ageGroup,
        @NotNull AssignmentType assignmentType,
        String inputData,
        String additionalRequirements
) {
}
