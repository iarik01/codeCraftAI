package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.codecrafters.task.domain.TaskSubmissionEntity;
import ru.codecrafters.task.domain.UserAccountEntity;

public record SubmissionResponse(
        UUID id,
        UUID taskId,
        UUID studentId,
        String studentName,
        String studentEmail,
        String answerText,
        String status,
        String teacherComment,
        Integer grade,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt,
        OffsetDateTime updatedAt
) {
    public static SubmissionResponse from(TaskSubmissionEntity submission, UserAccountEntity student) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getTaskId(),
                submission.getStudentId(),
                student == null ? null : student.getName(),
                student == null ? null : student.getEmail(),
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
