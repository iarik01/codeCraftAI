package ru.codecrafters.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.codecrafters.ai.web.dto.GenerateAssignmentRequest;
import ru.codecrafters.ai.web.dto.GeneratedAssignmentResponse;

@Service
public class MockAssignmentGenerator {
    private final boolean mockEnabled;

    public MockAssignmentGenerator(@Value("${app.ai.mock-enabled}") boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public GeneratedAssignmentResponse generate(GenerateAssignmentRequest request) {
        if (!mockEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Real GigaChat integration is not implemented yet");
        }

        String topic = request.topic().trim();
        String readableDirection = switch (request.direction()) {
            case SCRATCH -> "Scratch";
            case PYTHON -> "Python";
            case ALGORITHMS -> "алгоритмике";
            case HTML_CSS -> "HTML/CSS";
        };

        String extra = request.additionalRequirements() == null || request.additionalRequirements().isBlank()
                ? "Без дополнительных пожеланий."
                : "Дополнительные пожелания: " + request.additionalRequirements().trim();

        return new GeneratedAssignmentResponse(
                "Практика по теме: " + topic,
                "Ученику нужно выполнить учебное задание по направлению " + readableDirection + ". " + extra,
                "Закрепить тему \"" + topic + "\" на уровне " + request.difficulty().name() + ".",
                "1. Прочитай условие.\n2. Выполни задание самостоятельно.\n3. Проверь результат по критериям.\n4. Подготовь короткое описание решения.",
                "Готовая работа, демонстрирующая понимание темы \"" + topic + "\".",
                "Решение соответствует теме; выполнены основные требования; результат можно проверить вручную; оформление понятно ученику и преподавателю.",
                "Начни с простого примера, затем постепенно добавляй детали. Если задание кажется сложным, разбей его на маленькие шаги.",
                "Эталонный ответ зависит от выбранной среды. Преподаватель проверяет корректность идеи, полноту выполнения и объяснение ученика.",
                request.direction(),
                request.ageGroup(),
                request.difficulty(),
                request.assignmentType(),
                "MOCK"
        );
    }
}
