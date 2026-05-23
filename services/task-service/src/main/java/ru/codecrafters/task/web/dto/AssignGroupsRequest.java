package ru.codecrafters.task.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AssignGroupsRequest(
        @NotEmpty
        List<@NotNull UUID> groupIds,

        OffsetDateTime deadline
) {
}
