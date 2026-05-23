package ru.codecrafters.task.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codecrafters.task.security.JwtPrincipal;
import ru.codecrafters.task.security.JwtPrincipalResolver;
import ru.codecrafters.task.service.SubmissionReviewService;
import ru.codecrafters.task.service.TaskAssignmentService;
import ru.codecrafters.task.service.TaskService;
import ru.codecrafters.task.web.dto.AssignGroupsRequest;
import ru.codecrafters.task.web.dto.GenerateTaskRequest;
import ru.codecrafters.task.web.dto.SubmissionResponse;
import ru.codecrafters.task.web.dto.TaskGroupAssignmentResponse;
import ru.codecrafters.task.web.dto.TaskResponse;
import ru.codecrafters.task.web.dto.UpdateTaskContentRequest;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final TaskAssignmentService assignmentService;
    private final SubmissionReviewService submissionReviewService;
    private final JwtPrincipalResolver principalResolver;

    public TaskController(
            TaskService taskService,
            TaskAssignmentService assignmentService,
            SubmissionReviewService submissionReviewService,
            JwtPrincipalResolver principalResolver
    ) {
        this.taskService = taskService;
        this.assignmentService = assignmentService;
        this.submissionReviewService = submissionReviewService;
        this.principalResolver = principalResolver;
    }

    @PostMapping("/generate")
    public TaskResponse generate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody GenerateTaskRequest request
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return taskService.generate(teacher.userId(), request);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID id
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return taskService.findById(teacher.userId(), id);
    }

    @GetMapping
    public List<TaskResponse> getCurrentTeacherTasks(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return taskService.findByTeacherId(teacher.userId());
    }

    @PostMapping("/{taskId}/assign-groups")
    public List<TaskGroupAssignmentResponse> assignGroups(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignGroupsRequest request
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return assignmentService.assignGroups(teacher.userId(), taskId, request);
    }

    @PatchMapping("/{id}/content")
    public TaskResponse updateContent(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID id,
            @RequestBody UpdateTaskContentRequest request
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return taskService.updateContent(teacher.userId(), id, request);
    }

    @GetMapping("/{taskId}/submissions")
    public List<SubmissionResponse> findSubmissions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID taskId
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return submissionReviewService.findTaskSubmissions(teacher.userId(), taskId);
    }
}
