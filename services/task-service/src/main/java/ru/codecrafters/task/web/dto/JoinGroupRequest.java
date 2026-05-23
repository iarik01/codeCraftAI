package ru.codecrafters.task.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinGroupRequest(
        @NotBlank
        @Size(min = 6, max = 16)
        String inviteCode
) {
}
