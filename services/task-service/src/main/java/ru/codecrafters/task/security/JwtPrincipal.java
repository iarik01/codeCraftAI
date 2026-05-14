package ru.codecrafters.task.security;

import java.util.UUID;

public record JwtPrincipal(
        UUID userId,
        String email,
        String role
) {
    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }
}
