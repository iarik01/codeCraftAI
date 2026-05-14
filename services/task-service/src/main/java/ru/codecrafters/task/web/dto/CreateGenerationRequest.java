package ru.codecrafters.task.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

public record CreateGenerationRequest(
        @NotNull Direction direction,
        @NotBlank @Size(max = 255) String topic,
        @NotNull Difficulty difficulty,
        @NotNull AgeGroup ageGroup,
        @NotNull AssignmentType assignmentType,
        @Positive Integer questionsCount,
        String additionalRequirements
) {
}
