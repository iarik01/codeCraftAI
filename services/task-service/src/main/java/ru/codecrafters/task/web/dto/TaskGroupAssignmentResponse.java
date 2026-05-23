package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.codecrafters.task.domain.TaskGroupAssignmentEntity;

public record TaskGroupAssignmentResponse(
        UUID id,
        UUID taskId,
        UUID groupId,
        OffsetDateTime assignedAt,
        OffsetDateTime deadline
) {
    public static TaskGroupAssignmentResponse from(TaskGroupAssignmentEntity assignment) {
        return new TaskGroupAssignmentResponse(
                assignment.getId(),
                assignment.getTaskId(),
                assignment.getGroupId(),
                assignment.getAssignedAt(),
                assignment.getDeadline()
        );
    }
}
