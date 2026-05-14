package ru.codecrafters.task.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddStudentRequest(
        @NotBlank
        @Email
        String email
) {
}
