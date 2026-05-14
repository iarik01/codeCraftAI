package ru.codecrafters.task.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codecrafters.task.security.JwtPrincipal;
import ru.codecrafters.task.security.JwtPrincipalResolver;
import ru.codecrafters.task.service.StudentTaskService;
import ru.codecrafters.task.web.dto.StudentSubmissionResponse;
import ru.codecrafters.task.web.dto.StudentTaskResponse;
import ru.codecrafters.task.web.dto.SubmitTaskRequest;

@RestController
@RequestMapping("/api/student/tasks")
public class StudentTaskController {
    private final StudentTaskService studentTaskService;
    private final JwtPrincipalResolver principalResolver;

    public StudentTaskController(StudentTaskService studentTaskService, JwtPrincipalResolver principalResolver) {
        this.studentTaskService = studentTaskService;
        this.principalResolver = principalResolver;
    }

    @GetMapping
    public List<StudentTaskResponse> findAssignedTasks(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        JwtPrincipal student = principalResolver.requireStudent(authorization);
        return studentTaskService.findAssignedTasks(student.userId());
    }

    @GetMapping("/{taskId}")
    public StudentTaskResponse findAssignedTask(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID taskId
    ) {
        JwtPrincipal student = principalResolver.requireStudent(authorization);
        return studentTaskService.findAssignedTask(student.userId(), taskId);
    }

    @PostMapping("/{taskId}/submit")
    public StudentSubmissionResponse submit(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID taskId,
            @Valid @RequestBody SubmitTaskRequest request
    ) {
        JwtPrincipal student = principalResolver.requireStudent(authorization);
        return studentTaskService.submit(student.userId(), taskId, request);
    }
}
