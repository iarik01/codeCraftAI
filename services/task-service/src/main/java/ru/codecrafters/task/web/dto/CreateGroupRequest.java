package ru.codecrafters.task.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 2000)
        String description
) {
}
