package ru.codecrafters.task.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import ru.codecrafters.task.client.AiTaskClient;
import ru.codecrafters.task.domain.AiGenerationRequestEntity;
import ru.codecrafters.task.domain.GeneratedTaskEntity;
import ru.codecrafters.task.repository.AiGenerationRequestRepository;
import ru.codecrafters.task.repository.GeneratedTaskRepository;
import ru.codecrafters.task.model.AssignmentType;
import ru.codecrafters.task.web.dto.AiGenerateTaskRequest;
import ru.codecrafters.task.web.dto.AiGenerateTaskResponse;
import ru.codecrafters.task.web.dto.GenerateTaskRequest;
import ru.codecrafters.task.web.dto.TaskResponse;
import ru.codecrafters.task.web.dto.UpdateTaskContentRequest;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final GeneratedTaskRepository taskRepository;
    private final AiGenerationRequestRepository aiRequestRepository;
    private final AiTaskClient aiTaskClient;

    public TaskService(
            GeneratedTaskRepository taskRepository,
            AiGenerationRequestRepository aiRequestRepository,
            AiTaskClient aiTaskClient
    ) {
        this.taskRepository = taskRepository;
        this.aiRequestRepository = aiRequestRepository;
        this.aiTaskClient = aiTaskClient;
    }

    public TaskResponse generate(UUID teacherId, GenerateTaskRequest request) {
        String prompt = buildPrompt(request);
        GeneratedTaskEntity task = taskRepository.save(new GeneratedTaskEntity(
                teacherId,
                request.subjectArea(),
                request.topic().trim(),
                request.difficulty(),
                request.gradeLevel(),
                request.taskType(),
                prompt
        ));

        AiGenerateTaskRequest aiRequest = new AiGenerateTaskRequest(
                request.subjectArea(),
                request.topic().trim(),
                request.difficulty(),
                request.gradeLevel(),
                request.taskType(),
                request.inputData(),
                request.additionalRequirements()
        );

        try {
            log.info("Calling ai-service for taskId={}, topic={}", task.getId(), request.topic());
            AiGenerateTaskResponse aiResponse = aiTaskClient.generateTask(aiRequest);
            Map<String, Object> generatedContent = generatedContent(aiResponse, request.taskType());
            String aiProvider = aiResponse.aiProvider() != null ? aiResponse.aiProvider() : "UNKNOWN";

            task.markGenerated(generatedContent, aiProvider);
            GeneratedTaskEntity savedTask = taskRepository.save(task);
            aiRequestRepository.save(new AiGenerationRequestEntity(
                    savedTask,
                    requestPayload(aiRequest, prompt),
                    generatedContent,
                    "SUCCESS",
                    null
            ));

            return TaskResponse.from(savedTask);
        } catch (RestClientException exception) {
            log.error("ai-service is unavailable for taskId={}", task.getId(), exception);
            task.markFailed();
            GeneratedTaskEntity failedTask = taskRepository.save(task);
            aiRequestRepository.save(new AiGenerationRequestEntity(
                    failedTask,
                    requestPayload(aiRequest, prompt),
                    null,
                    "FAILED",
                    exception.getMessage()
            ));
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI service is unavailable");
        }
    }

    public TaskResponse findById(UUID teacherId, UUID id) {
        return taskRepository.findByIdAndTeacherId(id, teacherId)
                .map(TaskResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public List<TaskResponse> findByTeacherId(UUID teacherId) {
        return taskRepository.findAllByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse updateContent(UUID teacherId, UUID id, UpdateTaskContentRequest request) {
        GeneratedTaskEntity task = taskRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.updateContent(request.generatedContent());
        return TaskResponse.from(taskRepository.save(task));
    }

    private String buildPrompt(GenerateTaskRequest request) {
        StringBuilder prompt = new StringBuilder()
                .append("Сгенерируй учебное задание для образовательного SaaS-сервиса CodeCrafters AI.\n")
                .append("Направление: ").append(request.subjectArea()).append('\n')
                .append("Тема: ").append(request.topic().trim()).append('\n')
                .append("Сложность: ").append(request.difficulty()).append('\n')
                .append("Возраст/класс: ").append(request.gradeLevel()).append('\n')
                .append("Тип задания: ").append(request.taskType()).append('\n');

        if (request.inputData() != null && !request.inputData().isBlank()) {
            prompt.append("Входные данные: ").append(request.inputData().trim()).append('\n');
        }

        if (request.additionalRequirements() != null && !request.additionalRequirements().isBlank()) {
            prompt.append("Дополнительные пожелания: ").append(request.additionalRequirements().trim()).append('\n');
        }

        return prompt.toString();
    }

    private Map<String, Object> requestPayload(AiGenerateTaskRequest request, String prompt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("direction", request.direction());
        payload.put("topic", request.topic());
        payload.put("difficulty", request.difficulty());
        payload.put("ageGroup", request.ageGroup());
        payload.put("assignmentType", request.assignmentType());
        payload.put("inputData", request.inputData());
        payload.put("additionalRequirements", request.additionalRequirements());
        payload.put("prompt", prompt);
        return payload;
    }

    private Map<String, Object> generatedContent(AiGenerateTaskResponse response, AssignmentType requestedTaskType) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("title", response.title());
        content.put("description", response.description());
        content.put("instructions", response.instructions());
        content.put("inputData", response.inputData());
        content.put("expectedResult", response.expectedResult());
        content.put("hints", response.hints());
        content.put("difficulty", response.difficulty());
        content.put("topic", response.topic());
        content.putAll(response.additionalFields());
        content.put("taskType", requestedTaskType.name());
        return content;
    }
}
