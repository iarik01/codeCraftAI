package ru.codecrafters.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

@Entity
@Table(name = "assignment_generation_requests")
public class AssignmentGenerationRequestEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Direction direction;

    @Column(nullable = false, length = 255)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 16)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 64)
    private AssignmentType assignmentType;

    @Column(name = "questions_count")
    private Integer questionsCount;

    @Column(name = "additional_requirements")
    private String additionalRequirements;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AssignmentGenerationRequestEntity() {
    }

    public AssignmentGenerationRequestEntity(UUID teacherId, Direction direction, String topic, AgeGroup ageGroup,
                                             Difficulty difficulty, AssignmentType assignmentType,
                                             Integer questionsCount, String additionalRequirements) {
        this.teacherId = teacherId;
        this.direction = direction;
        this.topic = topic;
        this.ageGroup = ageGroup;
        this.difficulty = difficulty;
        this.assignmentType = assignmentType;
        this.questionsCount = questionsCount;
        this.additionalRequirements = additionalRequirements;
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getTopic() {
        return topic;
    }

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public Integer getQuestionsCount() {
        return questionsCount;
    }

    public String getAdditionalRequirements() {
        return additionalRequirements;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
