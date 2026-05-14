package ru.codecrafters.task.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.codecrafters.task.domain.GeneratedTaskEntity;
import ru.codecrafters.task.domain.GroupMemberEntity;
import ru.codecrafters.task.domain.TaskGroupAssignmentEntity;
import ru.codecrafters.task.domain.TaskSubmissionEntity;
import ru.codecrafters.task.repository.GeneratedTaskRepository;
import ru.codecrafters.task.repository.GroupMemberRepository;
import ru.codecrafters.task.repository.TaskGroupAssignmentRepository;
import ru.codecrafters.task.repository.TaskSubmissionRepository;
import ru.codecrafters.task.web.dto.StudentSubmissionResponse;
import ru.codecrafters.task.web.dto.StudentTaskResponse;
import ru.codecrafters.task.web.dto.SubmitTaskRequest;

@Service
public class StudentTaskService {
    private final GroupMemberRepository memberRepository;
    private final TaskGroupAssignmentRepository assignmentRepository;
    private final GeneratedTaskRepository taskRepository;
    private final TaskSubmissionRepository submissionRepository;

    public StudentTaskService(
            GroupMemberRepository memberRepository,
            TaskGroupAssignmentRepository assignmentRepository,
            GeneratedTaskRepository taskRepository,
            TaskSubmissionRepository submissionRepository
    ) {
        this.memberRepository = memberRepository;
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<StudentTaskResponse> findAssignedTasks(UUID studentId) {
        List<UUID> groupIds = studentGroupIds(studentId);
        if (groupIds.isEmpty()) {
            return List.of();
        }

        List<UUID> taskIds = assignmentRepository.findAllByGroupIdIn(groupIds).stream()
                .map(TaskGroupAssignmentEntity::getTaskId)
                .distinct()
                .toList();
        if (taskIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, TaskSubmissionEntity> submissionsByTaskId = submissionRepository
                .findAllByTaskIdInAndStudentId(taskIds, studentId)
                .stream()
                .collect(Collectors.toMap(TaskSubmissionEntity::getTaskId, Function.identity()));

        return taskRepository.findAllById(taskIds).stream()
                .filter(task -> "GENERATED".equals(task.getStatus()))
                .map(task -> StudentTaskResponse.from(task, submissionsByTaskId.get(task.getId())))
                .toList();
    }

    public StudentTaskResponse findAssignedTask(UUID studentId, UUID taskId) {
        ensureTaskAssignedToStudent(studentId, taskId);
        GeneratedTaskEntity task = taskRepository.findById(taskId)
                .filter(foundTask -> "GENERATED".equals(foundTask.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        TaskSubmissionEntity submission = submissionRepository.findByTaskIdAndStudentId(taskId, studentId).orElse(null);
        return StudentTaskResponse.from(task, submission);
    }

    public StudentSubmissionResponse submit(UUID studentId, UUID taskId, SubmitTaskRequest request) {
        ensureTaskAssignedToStudent(studentId, taskId);
        String answerText = request.answerText().trim();

        TaskSubmissionEntity submission = submissionRepository.findByTaskIdAndStudentId(taskId, studentId)
                .map(existing -> {
                    existing.resubmit(answerText);
                    return existing;
                })
                .orElseGet(() -> new TaskSubmissionEntity(taskId, studentId, answerText));

        return StudentSubmissionResponse.from(submissionRepository.save(submission));
    }

    private void ensureTaskAssignedToStudent(UUID studentId, UUID taskId) {
        List<UUID> groupIds = studentGroupIds(studentId);
        if (groupIds.isEmpty() || !assignmentRepository.existsByTaskIdAndGroupIdIn(taskId, groupIds)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
    }

    private List<UUID> studentGroupIds(UUID studentId) {
        return memberRepository.findAllByStudentId(studentId).stream()
                .map(GroupMemberEntity::getGroupId)
                .toList();
    }
}
