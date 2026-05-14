package ru.codecrafters.task.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.codecrafters.task.domain.GroupMemberEntity;
import ru.codecrafters.task.domain.UserAccountEntity;

public record StudentResponse(
        UUID id,
        String name,
        String email,
        OffsetDateTime joinedAt
) {
    public static StudentResponse from(UserAccountEntity student, GroupMemberEntity membership) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                membership.getJoinedAt()
        );
    }
}
