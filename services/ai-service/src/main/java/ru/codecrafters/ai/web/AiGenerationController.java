package ru.codecrafters.ai.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codecrafters.ai.service.TaskGenerationService;
import ru.codecrafters.ai.web.dto.GenerateAssignmentRequest;
import ru.codecrafters.ai.web.dto.GenerateTaskRequest;
import ru.codecrafters.ai.web.dto.GeneratedAssignmentResponse;
import ru.codecrafters.ai.web.dto.GeneratedTaskResponse;

@RestController
@RequestMapping("/api/ai")
public class AiGenerationController {
    private static final Logger log = LoggerFactory.getLogger(AiGenerationController.class);

    private final TaskGenerationService taskGenerationService;

    public AiGenerationController(TaskGenerationService taskGenerationService) {
        this.taskGenerationService = taskGenerationService;
    }

    @PostMapping("/generate-task")
    public GeneratedTaskResponse generateTask(@Valid @RequestBody GenerateTaskRequest request) {
        log.info("Received generate-task request for topic={}", request.topic());
        return taskGenerationService.generate(request);
    }

    @PostMapping("/generate")
    public GeneratedAssignmentResponse generate(@Valid @RequestBody GenerateAssignmentRequest request) {
        log.info("Received legacy generate request for topic={}", request.topic());
        GeneratedTaskResponse generatedTask = taskGenerationService.generate(new GenerateTaskRequest(
                request.direction(),
                request.topic(),
                request.difficulty(),
                request.ageGroup(),
                request.assignmentType(),
                null,
                request.additionalRequirements()
        ));

        return new GeneratedAssignmentResponse(
                generatedTask.title(),
                generatedTask.description(),
                "Закрепить тему \"" + generatedTask.topic() + "\".",
                generatedTask.instructions(),
                generatedTask.expectedResult(),
                "Проверить соответствие инструкции, корректность результата и самостоятельность объяснения.",
                String.join("\n", generatedTask.hints()),
                "Эталонное решение зависит от среды выполнения и проверяется преподавателем вручную.",
                request.direction(),
                request.ageGroup(),
                request.difficulty(),
                request.assignmentType(),
                "MOCK"
        );
    }
}
