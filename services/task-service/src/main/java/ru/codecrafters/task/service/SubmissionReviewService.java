package ru.codecrafters.task.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.codecrafters.task.domain.TaskSubmissionEntity;
import ru.codecrafters.task.domain.UserAccountEntity;
import ru.codecrafters.task.repository.GeneratedTaskRepository;
import ru.codecrafters.task.repository.TaskSubmissionRepository;
import ru.codecrafters.task.repository.UserAccountRepository;
import ru.codecrafters.task.web.dto.ReviewSubmissionRequest;
import ru.codecrafters.task.web.dto.SubmissionResponse;

@Service
public class SubmissionReviewService {
    private final GeneratedTaskRepository taskRepository;
    private final TaskSubmissionRepository submissionRepository;
    private final UserAccountRepository userRepository;

    public SubmissionReviewService(
            GeneratedTaskRepository taskRepository,
            TaskSubmissionRepository submissionRepository,
            UserAccountRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public List<SubmissionResponse> findTaskSubmissions(UUID teacherId, UUID taskId) {
        requireTeacherTask(teacherId, taskId);
        List<TaskSubmissionEntity> submissions = submissionRepository.findAllByTaskId(taskId);
        Map<UUID, UserAccountEntity> studentsById = userRepository.findAllById(
                        submissions.stream().map(TaskSubmissionEntity::getStudentId).toList()
                ).stream()
                .collect(Collectors.toMap(UserAccountEntity::getId, Function.identity()));

        return submissions.stream()
                .map(submission -> SubmissionResponse.from(submission, studentsById.get(submission.getStudentId())))
                .toList();
    }

    public SubmissionResponse review(UUID teacherId, UUID submissionId, ReviewSubmissionRequest request) {
        TaskSubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        requireTeacherTask(teacherId, submission.getTaskId());

        submission.review(request.status(), request.grade(), normalizeNullable(request.teacherComment()));
        TaskSubmissionEntity saved = submissionRepository.save(submission);
        UserAccountEntity student = userRepository.findById(saved.getStudentId()).orElse(null);
        return SubmissionResponse.from(saved, student);
    }

    private void requireTeacherTask(UUID teacherId, UUID taskId) {
        taskRepository.findByIdAndTeacherId(taskId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
