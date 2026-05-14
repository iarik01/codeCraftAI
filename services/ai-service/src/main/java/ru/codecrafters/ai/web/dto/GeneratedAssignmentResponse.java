package ru.codecrafters.ai.web.dto;

import ru.codecrafters.ai.model.AgeGroup;
import ru.codecrafters.ai.model.AssignmentType;
import ru.codecrafters.ai.model.Difficulty;
import ru.codecrafters.ai.model.Direction;

public record GeneratedAssignmentResponse(
        String title,
        String description,
        String goal,
        String instructions,
        String expectedResult,
        String evaluationCriteria,
        String hints,
        String teacherSolution,
        Direction direction,
        AgeGroup ageGroup,
        Difficulty difficulty,
        AssignmentType assignmentType,
        String source
) {
}
