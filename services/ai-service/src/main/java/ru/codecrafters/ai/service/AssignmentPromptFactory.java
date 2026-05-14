package ru.codecrafters.ai.service;

import org.springframework.stereotype.Component;
import ru.codecrafters.ai.web.dto.GenerateTaskRequest;

@Component
public class AssignmentPromptFactory {
    public String build(GenerateTaskRequest request) {
        return switch (request.assignmentType()) {
            case TEST -> buildTestPrompt(request);
            case BUG_FIX -> buildBugFixPrompt(request);
            case MINI_PROJECT -> buildMiniProjectPrompt(request);
            case HOMEWORK_WITH_CRITERIA -> buildHomeworkWithCriteriaPrompt(request);
            case PRACTICE -> buildPracticePrompt(request);
        };
    }

    private String buildPracticePrompt(GenerateTaskRequest request) {
        return basePrompt(request) + """

                Тип PRACTICE: сгенерируй обычное практическое задание на написание кода или создание решения.
                Дополнительные обязательные поля:
                {
                  "starterCode": "string",
                  "requirements": ["string"],
                  "example": { "input": "string", "output": "string" },
                  "teacherSolution": "string"
                }
                Требования: задание должно требовать написания кода; expectedResult должен быть конкретным результатом; BEGINNER не усложняй, ADVANCED сделай с большим числом условий.
                """;
    }

    private String buildTestPrompt(GenerateTaskRequest request) {
        return basePrompt(request) + """

                Тип TEST: сгенерируй тест, а не практическую задачу.
                Дополнительные обязательные поля:
                {
                  "questions": [
                    {
                      "question": "string",
                      "options": ["string", "string", "string", "string"],
                      "correctAnswer": "string",
                      "explanation": "string"
                    }
                  ],
                  "passingScore": "string"
                }
                Если в дополнительных пожеланиях указано количество вопросов, используй его. Если не указано, сделай 5 вопросов.
                У каждого вопроса ровно 4 варианта ответа. correctAnswer должен совпадать с одним из options.
                instructions должны объяснять, что нужно выбрать правильные ответы.
                """;
    }

    private String buildBugFixPrompt(GenerateTaskRequest request) {
        return basePrompt(request) + """

                Тип BUG_FIX: сгенерируй задание на исправление ошибки.
                Дополнительные обязательные поля:
                {
                  "buggyCode": "string",
                  "bugDescription": "string",
                  "expectedFixedBehavior": "string",
                  "teacherSolution": "string",
                  "commonMistakes": ["string"]
                }
                buggyCode должен содержать реальную ошибку по теме и уровню сложности.
                Для Python дай код на Python, для HTML_CSS HTML/CSS-фрагмент, для Scratch опиши ошибочную логику блоков.
                instructions должны просить ученика найти и исправить ошибку.
                """;
    }

    private String buildMiniProjectPrompt(GenerateTaskRequest request) {
        return basePrompt(request) + """

                Тип MINI_PROJECT: сгенерируй небольшую проектную работу, а не одно упражнение.
                Дополнительные обязательные поля:
                {
                  "projectGoal": "string",
                  "functionalRequirements": ["string"],
                  "steps": ["string"],
                  "acceptanceCriteria": ["string"],
                  "extensionIdeas": ["string"]
                }
                Нужны этапы выполнения, функциональные требования и критерии готовности.
                Для BEGINNER проект маленький, для ADVANCED можно добавить больше требований.
                """;
    }

    private String buildHomeworkWithCriteriaPrompt(GenerateTaskRequest request) {
        return basePrompt(request) + """

                Тип HOMEWORK_WITH_CRITERIA: сгенерируй домашнее задание с критериями оценки.
                Дополнительные обязательные поля:
                {
                  "homeworkTasks": ["string"],
                  "evaluationCriteria": [
                    { "criterion": "string", "points": 1 }
                  ],
                  "maxScore": 10,
                  "teacherNotes": "string"
                }
                Критерии должны суммарно давать maxScore=10. teacherNotes должны помогать преподавателю проверять работу.
                """;
    }

    private String basePrompt(GenerateTaskRequest request) {
        return """
                Верни только валидный JSON.
                Не используй Markdown.
                Не используй ```json.
                Не добавляй пояснения до или после JSON.
                Все строки должны быть на русском языке.
                JSON должен строго соответствовать указанной структуре.
                Все ключи обязательны. Не пропускай ключи, даже если значение неизвестно.
                Поле taskType должно быть строго "%s".
                Поле hints всегда должно быть массивом строк.
                Если входные данные не нужны или не заданы, верни "inputData": "Не требуется".

                Общий обязательный JSON:
                {
                  "title": "string",
                  "taskType": "%s",
                  "description": "string",
                  "instructions": "string",
                  "inputData": "string",
                  "expectedResult": "string",
                  "hints": ["string"],
                  "difficulty": "BEGINNER | INTERMEDIATE | ADVANCED",
                  "topic": "string"
                }

                Параметры задания:
                direction: %s
                topic: %s
                difficulty: %s
                ageGroup: %s
                assignmentType: %s
                inputData: %s
                additionalRequirements: %s
                """.formatted(
                request.assignmentType(),
                request.assignmentType(),
                request.direction(),
                request.topic(),
                request.difficulty(),
                request.ageGroup(),
                request.assignmentType(),
                blankToDefault(request.inputData(), "Не задано"),
                blankToDefault(request.additionalRequirements(), "Не задано")
        );
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
