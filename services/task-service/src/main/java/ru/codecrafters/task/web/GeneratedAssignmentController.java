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
import ru.codecrafters.task.service.AssignmentGenerationService;
import ru.codecrafters.task.web.dto.CreateGenerationRequest;
import ru.codecrafters.task.web.dto.GeneratedAssignmentResponse;

@RestController
@RequestMapping("/api/assignments")
public class GeneratedAssignmentController {
    private final AssignmentGenerationService assignmentGenerationService;
    private final JwtPrincipalResolver principalResolver;

    public GeneratedAssignmentController(
            AssignmentGenerationService assignmentGenerationService,
            JwtPrincipalResolver principalResolver
    ) {
        this.assignmentGenerationService = assignmentGenerationService;
        this.principalResolver = principalResolver;
    }

    @PostMapping("/generate")
    public GeneratedAssignmentResponse generate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody CreateGenerationRequest request
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return assignmentGenerationService.generate(teacher.userId(), request);
    }

    @GetMapping("/generated")
    public List<GeneratedAssignmentResponse> listGenerated(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return assignmentGenerationService.findAll(teacher.userId());
    }

    @GetMapping("/generated/{id}")
    public GeneratedAssignmentResponse getGenerated(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID id
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return assignmentGenerationService.findById(teacher.userId(), id);
    }
}
