package ru.codecrafters.task.web.dto;

import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

public record AiGenerateTaskRequest(
        Direction direction,
        String topic,
        Difficulty difficulty,
        AgeGroup ageGroup,
        AssignmentType assignmentType,
        String inputData,
        String additionalRequirements
) {
}
