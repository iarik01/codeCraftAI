package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.codecrafters.task.domain.TaskSubmissionEntity;

public record StudentSubmissionResponse(
        UUID id,
        UUID taskId,
        String answerText,
        String status,
        String teacherComment,
        Integer grade,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt,
        OffsetDateTime updatedAt
) {
    public static StudentSubmissionResponse from(TaskSubmissionEntity submission) {
        if (submission == null) {
            return null;
        }
        return new StudentSubmissionResponse(
                submission.getId(),
                submission.getTaskId(),
                submission.getAnswerText(),
                submission.getStatus(),
                submission.getTeacherComment(),
                submission.getGrade(),
                submission.getSubmittedAt(),
                submission.getReviewedAt(),
                submission.getUpdatedAt()
        );
    }
}
