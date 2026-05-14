package ru.codecrafters.task.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReviewSubmissionRequest(
        @Pattern(regexp = "REVIEWED")
        String status,

        @Min(1)
        @Max(5)
        Integer grade,

        @Size(max = 5000)
        String teacherComment
) {
}
