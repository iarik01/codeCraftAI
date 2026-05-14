package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.codecrafters.task.domain.GeneratedTaskEntity;
import ru.codecrafters.task.domain.TaskSubmissionEntity;
import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

public record StudentTaskResponse(
        UUID id,
        Direction subjectArea,
        String topic,
        Difficulty difficulty,
        AgeGroup gradeLevel,
        AssignmentType taskType,
        Map<String, Object> generatedContent,
        String status,
        OffsetDateTime createdAt,
        StudentSubmissionResponse submission
) {
    public static StudentTaskResponse from(GeneratedTaskEntity task, TaskSubmissionEntity submission) {
        return new StudentTaskResponse(
                task.getId(),
                task.getSubjectArea(),
                task.getTopic(),
                task.getDifficulty(),
                task.getGradeLevel(),
                task.getTaskType(),
                sanitizeContent(task.getGeneratedContent()),
                task.getStatus(),
                task.getCreatedAt(),
                StudentSubmissionResponse.from(submission)
        );
    }

    private static Map<String, Object> sanitizeContent(Map<String, Object> content) {
        if (content == null) {
            return Map.of();
        }

        Map<String, Object> sanitized = new LinkedHashMap<>(content);
        sanitized.remove("prompt");
        sanitized.remove("teacherId");
        sanitized.remove("aiProvider");
        sanitized.remove("teacherSolution");
        sanitized.remove("teacherNotes");
        sanitizeTestQuestions(sanitized);
        return sanitized;
    }

    private static void sanitizeTestQuestions(Map<String, Object> content) {
        Object questions = content.get("questions");
        if (!(questions instanceof Iterable<?> iterable)) {
            return;
        }

        List<Object> sanitizedQuestions = new ArrayList<>();
        for (Object question : iterable) {
            if (question instanceof Map<?, ?> map) {
                Map<String, Object> sanitizedQuestion = new LinkedHashMap<>();
                map.forEach((key, value) -> {
                    if (!"correctAnswer".equals(key) && !"explanation".equals(key)) {
                        sanitizedQuestion.put(String.valueOf(key), value);
                    }
                });
                sanitizedQuestions.add(sanitizedQuestion);
            } else {
                sanitizedQuestions.add(question);
            }
        }
        content.put("questions", sanitizedQuestions);
    }
}
