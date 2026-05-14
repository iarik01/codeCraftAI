package ru.codecrafters.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

@Entity
@Table(name = "generated_tasks")
public class GeneratedTaskEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_area", nullable = false, length = 32)
    private Direction subjectArea;

    @Column(nullable = false, length = 255)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level", nullable = false, length = 32)
    private AgeGroup gradeLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 64)
    private AssignmentType taskType;

    @Column(nullable = false)
    private String prompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "generated_content", columnDefinition = "jsonb")
    private Map<String, Object> generatedContent;

    @Column(name = "ai_provider", nullable = false, length = 32)
    private String aiProvider;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected GeneratedTaskEntity() {
    }

    public GeneratedTaskEntity(UUID teacherId, Direction subjectArea, String topic, Difficulty difficulty,
                               AgeGroup gradeLevel, AssignmentType taskType, String prompt) {
        this.teacherId = teacherId;
        this.subjectArea = subjectArea;
        this.topic = topic;
        this.difficulty = difficulty;
        this.gradeLevel = gradeLevel;
        this.taskType = taskType;
        this.prompt = prompt;
        this.aiProvider = "MOCK";
        this.status = "GENERATING";
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void markGenerated(Map<String, Object> generatedContent, String aiProvider) {
        this.generatedContent = generatedContent;
        this.aiProvider = aiProvider;
        this.status = "GENERATED";
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public UUID getId() { return id; }
    public UUID getTeacherId() { return teacherId; }
    public Direction getSubjectArea() { return subjectArea; }
    public String getTopic() { return topic; }
    public Difficulty getDifficulty() { return difficulty; }
    public AgeGroup getGradeLevel() { return gradeLevel; }
    public AssignmentType getTaskType() { return taskType; }
    public String getPrompt() { return prompt; }
    public Map<String, Object> getGeneratedContent() { return generatedContent; }
    public String getAiProvider() { return aiProvider; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
