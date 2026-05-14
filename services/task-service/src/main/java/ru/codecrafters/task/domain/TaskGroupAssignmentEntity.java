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
@Table(name = "task_group_assignments")
public class TaskGroupAssignmentEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    protected TaskGroupAssignmentEntity() {
    }

    public TaskGroupAssignmentEntity(UUID taskId, UUID groupId) {
        this.taskId = taskId;
        this.groupId = groupId;
    }

    @PrePersist
    void onCreate() {
        assignedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }
}
