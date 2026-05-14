package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import ru.codecrafters.task.domain.GeneratedTaskEntity;
import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

public record TaskResponse(
        UUID id,
        UUID teacherId,
        Direction subjectArea,
        String topic,
        Difficulty difficulty,
        AgeGroup gradeLevel,
        AssignmentType taskType,
        String prompt,
        Map<String, Object> generatedContent,
        String aiProvider,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TaskResponse from(GeneratedTaskEntity task) {
        return new TaskResponse(
                task.getId(),
                task.getTeacherId(),
                task.getSubjectArea(),
                task.getTopic(),
                task.getDifficulty(),
                task.getGradeLevel(),
                task.getTaskType(),
                task.getPrompt(),
                task.getGeneratedContent(),
                task.getAiProvider(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
