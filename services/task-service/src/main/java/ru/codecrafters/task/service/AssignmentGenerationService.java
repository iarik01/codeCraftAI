package ru.codecrafters.task.service;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.codecrafters.task.client.AiGenerationClient;
import ru.codecrafters.task.domain.AssignmentGenerationRequestEntity;
import ru.codecrafters.task.domain.GeneratedAssignmentEntity;
import ru.codecrafters.task.repository.AssignmentGenerationRequestRepository;
import ru.codecrafters.task.repository.GeneratedAssignmentRepository;
import ru.codecrafters.task.web.dto.AiGeneratedAssignmentResponse;
import ru.codecrafters.task.web.dto.CreateGenerationRequest;
import ru.codecrafters.task.web.dto.GeneratedAssignmentResponse;

@Service
public class AssignmentGenerationService {
    private final AssignmentGenerationRequestRepository requestRepository;
    private final GeneratedAssignmentRepository assignmentRepository;
    private final AiGenerationClient aiGenerationClient;

    public AssignmentGenerationService(
            AssignmentGenerationRequestRepository requestRepository,
            GeneratedAssignmentRepository assignmentRepository,
            AiGenerationClient aiGenerationClient
    ) {
        this.requestRepository = requestRepository;
        this.assignmentRepository = assignmentRepository;
        this.aiGenerationClient = aiGenerationClient;
    }

    @Transactional
    public GeneratedAssignmentResponse generate(UUID teacherId, CreateGenerationRequest request) {
        AssignmentGenerationRequestEntity savedRequest = requestRepository.save(new AssignmentGenerationRequestEntity(
                teacherId,
                request.direction(),
                request.topic().trim(),
                request.ageGroup(),
                request.difficulty(),
                request.assignmentType(),
                request.questionsCount(),
                request.additionalRequirements()
        ));

        AiGeneratedAssignmentResponse generated = aiGenerationClient.generate(request);

        GeneratedAssignmentEntity savedAssignment = assignmentRepository.save(new GeneratedAssignmentEntity(
                teacherId,
                savedRequest.getId(),
                generated.title(),
                generated.description(),
                generated.goal(),
                generated.instructions(),
                generated.expectedResult(),
                generated.evaluationCriteria(),
                generated.hints(),
                generated.teacherSolution(),
                generated.direction(),
                generated.ageGroup(),
                generated.difficulty(),
                generated.assignmentType(),
                generated.source()
        ));

        return GeneratedAssignmentResponse.from(savedAssignment);
    }

    @Transactional(readOnly = true)
    public List<GeneratedAssignmentResponse> findAll(UUID teacherId) {
        return assignmentRepository.findAllByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(GeneratedAssignmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GeneratedAssignmentResponse findById(UUID teacherId, UUID assignmentId) {
        return assignmentRepository.findByIdAndTeacherId(assignmentId, teacherId)
                .map(GeneratedAssignmentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Generated assignment not found"));
    }
}
