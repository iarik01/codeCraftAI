package ru.codecrafters.task.web;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codecrafters.task.security.JwtPrincipal;
import ru.codecrafters.task.security.JwtPrincipalResolver;
import ru.codecrafters.task.service.SubmissionReviewService;
import ru.codecrafters.task.web.dto.ReviewSubmissionRequest;
import ru.codecrafters.task.web.dto.SubmissionResponse;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionReviewService submissionReviewService;
    private final JwtPrincipalResolver principalResolver;

    public SubmissionController(SubmissionReviewService submissionReviewService, JwtPrincipalResolver principalResolver) {
        this.submissionReviewService = submissionReviewService;
        this.principalResolver = principalResolver;
    }

    @PatchMapping("/{submissionId}/review")
    public SubmissionResponse review(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID submissionId,
            @Valid @RequestBody ReviewSubmissionRequest request
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return submissionReviewService.review(teacher.userId(), submissionId, request);
    }
}
