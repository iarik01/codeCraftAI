package ru.codecrafters.ai.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        return formatInstructions() + formatParams(request) + """

                Верни JSON строго следующей структуры (все ключи обязательны, значения на русском):
                {
                  "title": "Краткое название задания",
                  "taskType": "PRACTICE",
                  "description": "Подробное описание задания для ученика",
                  "instructions": "Пошаговые инструкции что нужно сделать",
                  "inputData": "Входные данные или Не требуется",
                  "expectedResult": "Конкретный ожидаемый результат выполнения",
                  "hints": ["Подсказка 1", "Подсказка 2", "Подсказка 3"],
                  "difficulty": "%s",
                  "topic": "Точная тема задания",
                  "starterCode": "# Стартовый код\\ndef solution():\\n    pass",
                  "requirements": ["Требование 1", "Требование 2", "Требование 3"],
                  "example": {
                    "input": "Пример входных данных",
                    "output": "Ожидаемый вывод программы"
                  },
                  "teacherSolution": "# Полное решение\\ndef solution():\\n    return result"
                }
                """.formatted(request.difficulty());
    }

    private String buildTestPrompt(GenerateTaskRequest request) {
        int count = requestedQuestionCount(request.additionalRequirements());
        int passing = Math.max(1, (int) Math.ceil(count * 0.7));
        return formatInstructions() + formatParams(request) + """

                Сгенерируй тест из %d вопросов. У каждого вопроса — ровно 4 варианта ответа, только один правильный.

                Верни JSON строго следующей структуры (все ключи обязательны, значения на русском):
                {
                  "title": "Краткое название теста",
                  "taskType": "TEST",
                  "description": "О чём этот тест и какие знания проверяет",
                  "instructions": "Выберите один правильный ответ в каждом вопросе",
                  "inputData": "Не требуется",
                  "expectedResult": "Правильные ответы на все %d вопросов",
                  "hints": ["Подсказка 1", "Подсказка 2"],
                  "difficulty": "%s",
                  "topic": "Точная тема теста",
                  "questions": [
                    {
                      "question": "Текст вопроса?",
                      "options": ["Вариант А", "Вариант Б", "Вариант В", "Вариант Г"],
                      "correctAnswer": "Вариант А",
                      "explanation": "Объяснение почему этот ответ правильный"
                    }
                  ],
                  "passingScore": "%d из %d"
                }

                ВАЖНО: массив questions должен содержать ровно %d объектов. Поле correctAnswer должно дословно совпадать с одним из значений в массиве options.
                """.formatted(count, count, request.difficulty(), passing, count, count);
    }

    private String buildBugFixPrompt(GenerateTaskRequest request) {
        return formatInstructions() + formatParams(request) + """

                Верни JSON строго следующей структуры (все ключи обязательны, значения на русском):
                {
                  "title": "Краткое название задания на исправление ошибки",
                  "taskType": "BUG_FIX",
                  "description": "Описание задания: что делает код и какую ошибку нужно найти",
                  "instructions": "Найдите ошибку в коде, исправьте её и объясните что было не так",
                  "inputData": "Входные данные для проверки или Не требуется",
                  "expectedResult": "Как должен работать исправленный код",
                  "hints": ["Подсказка 1", "Подсказка 2", "Подсказка 3"],
                  "difficulty": "%s",
                  "topic": "Точная тема задания",
                  "buggyCode": "# Код с намеренной ошибкой\\nfor i in range(10)\\n    print(i)",
                  "bugDescription": "Подробное описание в чём заключается ошибка",
                  "expectedFixedBehavior": "Описание как должен работать исправленный код",
                  "commonMistakes": ["Типичная ошибка при попытке исправления 1", "Типичная ошибка 2"],
                  "teacherSolution": "# Исправленный код\\nfor i in range(10):\\n    print(i)"
                }

                ВАЖНО: buggyCode должен содержать реальную ошибку по теме «%s» уровня %s. Для Python — код на Python, для HTML_CSS — HTML/CSS фрагмент.
                """.formatted(request.difficulty(), request.topic(), request.difficulty());
    }

    private String buildMiniProjectPrompt(GenerateTaskRequest request) {
        return formatInstructions() + formatParams(request) + """

                Верни JSON строго следующей структуры (все ключи обязательны, значения на русском):
                {
                  "title": "Краткое название мини-проекта",
                  "taskType": "MINI_PROJECT",
                  "description": "Общее описание проекта и что нужно создать",
                  "instructions": "Общие инструкции по выполнению проекта",
                  "inputData": "Входные данные или Не требуется",
                  "expectedResult": "Что должно получиться в итоге",
                  "hints": ["Подсказка 1", "Подсказка 2", "Подсказка 3"],
                  "difficulty": "%s",
                  "topic": "Точная тема проекта",
                  "projectGoal": "Конкретная цель — что именно нужно создать",
                  "functionalRequirements": ["Функциональное требование 1", "Функциональное требование 2", "Функциональное требование 3"],
                  "steps": ["Шаг 1: описание", "Шаг 2: описание", "Шаг 3: описание", "Шаг 4: описание"],
                  "acceptanceCriteria": ["Критерий готовности 1", "Критерий готовности 2", "Критерий готовности 3"],
                  "extensionIdeas": ["Идея для развития проекта 1", "Идея для развития проекта 2"]
                }
                """.formatted(request.difficulty());
    }

    private String buildHomeworkWithCriteriaPrompt(GenerateTaskRequest request) {
        return formatInstructions() + formatParams(request) + """

                Верни JSON строго следующей структуры (все ключи обязательны, значения на русском):
                {
                  "title": "Краткое название домашнего задания",
                  "taskType": "HOMEWORK_WITH_CRITERIA",
                  "description": "Описание домашнего задания и его цели",
                  "instructions": "Инструкции по выполнению и сдаче работы",
                  "inputData": "Входные данные или Не требуется",
                  "expectedResult": "Что ученик должен сдать по итогу",
                  "hints": ["Подсказка 1", "Подсказка 2"],
                  "difficulty": "%s",
                  "topic": "Точная тема задания",
                  "homeworkTasks": ["Пункт 1: ...", "Пункт 2: ...", "Пункт 3: ...", "Пункт 4: ..."],
                  "evaluationCriteria": [
                    {"criterion": "Критерий оценки 1", "points": 4},
                    {"criterion": "Критерий оценки 2", "points": 3},
                    {"criterion": "Критерий оценки 3", "points": 3}
                  ],
                  "maxScore": 10,
                  "teacherNotes": "Советы преподавателю по проверке работы"
                }

                ВАЖНО: сумма значений points во всех объектах evaluationCriteria должна равняться maxScore (10). Поле points — целое число.
                """.formatted(request.difficulty());
    }

    private String formatInstructions() {
        return """
                Верни ТОЛЬКО валидный JSON. Без Markdown, без ```json, без пояснений до или после JSON.
                Все строки только на русском языке. Поле hints — всегда массив строк (минимум 2 элемента).
                """;
    }

    private String formatParams(GenerateTaskRequest request) {
        return """

                Параметры задания:
                - Направление: %s
                - Тема: %s
                - Сложность: %s
                - Возрастная группа: %s
                - Входные данные: %s
                - Дополнительные пожелания: %s
                """.formatted(
                request.direction(),
                request.topic(),
                request.difficulty(),
                request.ageGroup(),
                blankToDefault(request.inputData(), "Не заданы"),
                blankToDefault(request.additionalRequirements(), "Не заданы")
        );
    }

    int requestedQuestionCount(String additionalRequirements) {
        if (additionalRequirements == null || additionalRequirements.isBlank()) {
            return 5;
        }
        Matcher matcher = Pattern.compile("(\\d+)\\s*(вопрос|вопроса|вопросов)", Pattern.CASE_INSENSITIVE)
                .matcher(additionalRequirements);
        if (!matcher.find()) {
            return 5;
        }
        int count = Integer.parseInt(matcher.group(1));
        return Math.max(1, Math.min(count, 30));
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
