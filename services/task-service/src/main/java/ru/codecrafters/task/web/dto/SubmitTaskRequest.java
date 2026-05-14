package ru.codecrafters.task.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitTaskRequest(
        @NotBlank
        @Size(max = 10000)
        String answerText
) {
}
