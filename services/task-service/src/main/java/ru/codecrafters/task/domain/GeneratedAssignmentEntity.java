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
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import ru.codecrafters.task.model.AgeGroup;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.model.Difficulty;
import ru.codecrafters.task.model.Direction;

@Entity
@Table(name = "generated_assignments")
public class GeneratedAssignmentEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "generation_request_id")
    private UUID generationRequestId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private String description;

    private String goal;

    @Column(nullable = false)
    private String instructions;

    @Column(name = "expected_result")
    private String expectedResult;

    @Column(name = "evaluation_criteria")
    private String evaluationCriteria;

    private String hints;

    @Column(name = "teacher_solution")
    private String teacherSolution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 16)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 64)
    private AssignmentType assignmentType;

    @Column(nullable = false, length = 32)
    private String source;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected GeneratedAssignmentEntity() {
    }

    public GeneratedAssignmentEntity(UUID teacherId, UUID generationRequestId, String title, String description,
                                     String goal, String instructions, String expectedResult,
                                     String evaluationCriteria, String hints, String teacherSolution,
                                     Direction direction, AgeGroup ageGroup, Difficulty difficulty,
                                     AssignmentType assignmentType, String source) {
        this.teacherId = teacherId;
        this.generationRequestId = generationRequestId;
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.instructions = instructions;
        this.expectedResult = expectedResult;
        this.evaluationCriteria = evaluationCriteria;
        this.hints = hints;
        this.teacherSolution = teacherSolution;
        this.direction = direction;
        this.ageGroup = ageGroup;
        this.difficulty = difficulty;
        this.assignmentType = assignmentType;
        this.source = source;
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

    public UUID getId() { return id; }
    public UUID getTeacherId() { return teacherId; }
    public UUID getGenerationRequestId() { return generationRequestId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getGoal() { return goal; }
    public String getInstructions() { return instructions; }
    public String getExpectedResult() { return expectedResult; }
    public String getEvaluationCriteria() { return evaluationCriteria; }
    public String getHints() { return hints; }
    public String getTeacherSolution() { return teacherSolution; }
    public Direction getDirection() { return direction; }
    public AgeGroup getAgeGroup() { return ageGroup; }
    public Difficulty getDifficulty() { return difficulty; }
    public AssignmentType getAssignmentType() { return assignmentType; }
    public String getSource() { return source; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
