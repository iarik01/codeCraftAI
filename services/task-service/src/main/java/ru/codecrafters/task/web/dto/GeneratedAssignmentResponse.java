package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.codecrafters.task.domain.GeneratedAssignmentEntity;
import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

public record GeneratedAssignmentResponse(
        UUID id,
        UUID generationRequestId,
        UUID teacherId,
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
        String source,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static GeneratedAssignmentResponse from(GeneratedAssignmentEntity assignment) {
        return new GeneratedAssignmentResponse(
                assignment.getId(),
                assignment.getGenerationRequestId(),
                assignment.getTeacherId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getGoal(),
                assignment.getInstructions(),
                assignment.getExpectedResult(),
                assignment.getEvaluationCriteria(),
                assignment.getHints(),
                assignment.getTeacherSolution(),
                assignment.getDirection(),
                assignment.getAgeGroup(),
                assignment.getDifficulty(),
                assignment.getAssignmentType(),
                assignment.getSource(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}
