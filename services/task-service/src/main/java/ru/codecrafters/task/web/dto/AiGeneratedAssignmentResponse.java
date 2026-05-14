package ru.codecrafters.task.web.dto;

import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

public record AiGeneratedAssignmentResponse(
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
