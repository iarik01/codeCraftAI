package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.codecrafters.task.domain.GroupEntity;

public record GroupResponse(
        UUID id,
        UUID teacherId,
        String name,
        String description,
        String inviteCode,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static GroupResponse from(GroupEntity group) {
        return new GroupResponse(
                group.getId(),
                group.getTeacherId(),
                group.getName(),
                group.getDescription(),
                group.getInviteCode(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
