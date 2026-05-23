package ru.codecrafters.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "task_submissions")
public class TaskSubmissionEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "answer_text", nullable = false)
    private String answerText;

    @Column(name = "answer_url")
    private String answerUrl;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "teacher_comment")
    private String teacherComment;

    @Column
    private Integer grade;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected TaskSubmissionEntity() {
    }

    public TaskSubmissionEntity(UUID taskId, UUID studentId, String answerText, String answerUrl) {
        this.taskId = taskId;
        this.studentId = studentId;
        this.answerText = answerText;
        this.answerUrl = answerUrl;
        this.status = "SUBMITTED";
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        submittedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void resubmit(String answerText, String answerUrl) {
        this.answerText = answerText;
        this.answerUrl = answerUrl;
        this.status = "SUBMITTED";
        this.teacherComment = null;
        this.grade = null;
        this.reviewedAt = null;
        this.submittedAt = OffsetDateTime.now();
    }

    public void review(String status, Integer grade, String teacherComment) {
        this.status = status;
        this.grade = grade;
        this.teacherComment = teacherComment;
        this.reviewedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTaskId() { return taskId; }
    public UUID getStudentId() { return studentId; }
    public String getAnswerText() { return answerText; }
    public String getAnswerUrl() { return answerUrl; }
    public String getStatus() { return status; }
    public String getTeacherComment() { return teacherComment; }
    public Integer getGrade() { return grade; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
