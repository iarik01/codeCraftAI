package ru.codecrafters.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "group_members")
public class GroupMemberEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    protected GroupMemberEntity() {
    }

    public GroupMemberEntity(UUID groupId, UUID studentId) {
        this.groupId = groupId;
        this.studentId = studentId;
    }

    @PrePersist
    void onCreate() {
        joinedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}
