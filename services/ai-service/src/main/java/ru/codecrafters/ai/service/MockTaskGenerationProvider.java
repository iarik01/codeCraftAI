package ru.codecrafters.ai.service;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import ru.codecrafters.ai.web.dto.GenerateTaskRequest;
import ru.codecrafters.ai.web.dto.GeneratedTaskResponse;

@Component
public class MockTaskGenerationProvider implements TaskGenerationProvider {
    @Override
    public GeneratedTaskResponse generate(GenerateTaskRequest request) {
        String topic = request.topic().trim();
        String inputData = request.inputData() == null || request.inputData().isBlank()
                ? "Не требуется. Ученик придумывает примеры самостоятельно."
                : request.inputData().trim();

        String extra = request.additionalRequirements() == null || request.additionalRequirements().isBlank()
                ? ""
                : " Учти пожелание преподавателя: " + request.additionalRequirements().trim();

        return switch (request.assignmentType()) {
            case TEST -> test(topic, inputData, extra, request);
            case BUG_FIX -> bugFix(topic, inputData, extra, request);
            case MINI_PROJECT -> miniProject(topic, inputData, extra, request);
            case HOMEWORK_WITH_CRITERIA -> homework(topic, inputData, extra, request);
            case PRACTICE -> practice(topic, inputData, extra, request);
        };
    }

    private GeneratedTaskResponse practice(String topic, String inputData, String extra, GenerateTaskRequest request) {
        return new GeneratedTaskResponse(
                "Практика: " + topic,
                "PRACTICE",
                "Практическое задание на написание кода по теме \"" + topic + "\" для направления " + request.direction() + "." + extra,
                "Напиши программу или решение по шагам, проверь результат и кратко объясни подход.",
                inputData,
                "Рабочий код решает задачу и выводит ожидаемый результат для примера.",
                defaultHints(),
                request.difficulty().name(),
                topic,
                "MOCK",
                Map.of(
                        "starterCode", "# Напиши решение здесь\n",
                        "requirements", List.of("Использовать тему " + topic, "Добавить понятные имена переменных", "Проверить решение на примере"),
                        "example", Map.of("input", inputData, "output", "Корректный результат работы программы"),
                        "teacherSolution", "Пример решения зависит от конкретной среды и проверяется преподавателем."
                )
        );
    }

    private GeneratedTaskResponse test(String topic, String inputData, String extra, GenerateTaskRequest request) {
        int questionCount = requestedQuestionCount(request.additionalRequirements());
        return new GeneratedTaskResponse(
                "Тест: " + topic,
                "TEST",
                "Тест проверяет понимание темы \"" + topic + "\"." + extra,
                "Выбери один правильный ответ в каждом вопросе.",
                inputData,
                "Тест пройден, если ученик набрал проходной балл.",
                defaultHints(),
                request.difficulty().name(),
                topic,
                "MOCK",
                Map.of(
                        "questions", IntStream.rangeClosed(1, questionCount)
                                .mapToObj(index -> question(index, topic))
                                .toList(),
                        "passingScore", Math.max(1, (int) Math.ceil(questionCount * 0.7)) + " из " + questionCount
                )
        );
    }

    private GeneratedTaskResponse bugFix(String topic, String inputData, String extra, GenerateTaskRequest request) {
        return new GeneratedTaskResponse(
                "Исправь ошибку: " + topic,
                "BUG_FIX",
                "Задание на поиск и исправление ошибки по теме \"" + topic + "\"." + extra,
                "Найди ошибку в коде, исправь её и объясни, почему исправление работает.",
                inputData,
                "Исправленный код работает без ошибки и выдаёт правильный результат.",
                defaultHints(),
                request.difficulty().name(),
                topic,
                "MOCK",
                Map.of(
                        "buggyCode", "count = 0\nfor number in range(1, 6):\ncount = count + number\nprint(count)",
                        "bugDescription", "Внутри цикла нарушен отступ, поэтому код не выполняется корректно.",
                        "expectedFixedBehavior", "Программа суммирует числа от 1 до 5 и выводит 15.",
                        "teacherSolution", "count = 0\nfor number in range(1, 6):\n    count = count + number\nprint(count)",
                        "commonMistakes", List.of("Исправить только print", "Убрать цикл вместо исправления отступа")
                )
        );
    }

    private GeneratedTaskResponse miniProject(String topic, String inputData, String extra, GenerateTaskRequest request) {
        return new GeneratedTaskResponse(
                "Мини-проект: " + topic,
                "MINI_PROJECT",
                "Небольшая проектная работа по теме \"" + topic + "\"." + extra,
                "Выполни проект по этапам и проверь каждый критерий готовности.",
                inputData,
                "Готовый мини-проект работает и демонстрирует тему " + topic + ".",
                defaultHints(),
                request.difficulty().name(),
                topic,
                "MOCK",
                Map.of(
                        "projectGoal", "Создать небольшой проект, который показывает понимание темы " + topic + ".",
                        "functionalRequirements", List.of("Есть ввод или исходные данные", "Есть обработка данных", "Есть понятный результат"),
                        "steps", List.of("Спланируй проект", "Напиши первую версию", "Проверь результат", "Улучши оформление"),
                        "acceptanceCriteria", List.of("Проект запускается", "Результат соответствует условию", "Код понятен"),
                        "extensionIdeas", List.of("Добавить больше примеров", "Улучшить интерфейс")
                )
        );
    }

    private GeneratedTaskResponse homework(String topic, String inputData, String extra, GenerateTaskRequest request) {
        return new GeneratedTaskResponse(
                "Домашнее задание: " + topic,
                "HOMEWORK_WITH_CRITERIA",
                "Домашнее задание с критериями оценки по теме \"" + topic + "\"." + extra,
                "Выполни все пункты задания и подготовь краткое объяснение решения.",
                inputData,
                "Ученик сдаёт решение и объяснение по каждому пункту домашнего задания.",
                defaultHints(),
                request.difficulty().name(),
                topic,
                "MOCK",
                Map.of(
                        "homeworkTasks", List.of("Повтори основные понятия темы", "Выполни практическую часть", "Проверь решение", "Напиши короткое объяснение"),
                        "evaluationCriteria", List.of(
                                Map.of("criterion", "Корректность решения", "points", 4),
                                Map.of("criterion", "Использование темы " + topic, "points", 3),
                                Map.of("criterion", "Понятное объяснение", "points", 3)
                        ),
                        "maxScore", 10,
                        "teacherNotes", "Проверь, что ученик понимает тему, а не просто переписал пример."
                )
        );
    }

    private Map<String, Object> question(int index, String topic) {
        String correct = "Правильное утверждение по теме " + topic;
        return Map.of(
                "question", "Вопрос " + index + " по теме \"" + topic + "\"",
                "options", List.of(correct, "Неверный вариант 1", "Неверный вариант 2", "Неверный вариант 3"),
                "correctAnswer", correct,
                "explanation", "Правильный ответ связан с базовым пониманием темы."
        );
    }

    private int requestedQuestionCount(String additionalRequirements) {
        if (additionalRequirements == null || additionalRequirements.isBlank()) {
            return 5;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)\\s*(вопрос|вопроса|вопросов)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(additionalRequirements);
        if (!matcher.find()) {
            return 5;
        }
        int count = Integer.parseInt(matcher.group(1));
        return Math.max(1, Math.min(count, 30));
    }

    private List<String> defaultHints() {
        return List.of(
                "Начни с самого простого варианта решения.",
                "Проверь результат на нескольких примерах.",
                "Если возникла ошибка, сравни каждый шаг с инструкцией."
        );
    }
}
