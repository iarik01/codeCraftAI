package ru.codecrafters.auth.web.dto;

import java.util.UUID;
import ru.codecrafters.auth.domain.RoleCode;
import ru.codecrafters.auth.domain.User;

public record UserResponse(
        UUID id,
        String name,
        String email,
        RoleCode role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getCode()
        );
    }
}
