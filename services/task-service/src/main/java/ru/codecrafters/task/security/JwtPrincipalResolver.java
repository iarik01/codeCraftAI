package ru.codecrafters.task.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtPrincipalResolver {
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;

    public JwtPrincipalResolver(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public JwtPrincipal requirePrincipal(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(authorizationHeader.substring(BEARER_PREFIX.length()))
                    .getPayload();

            if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is expired");
            }

            return new JwtPrincipal(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("role", String.class)
            );
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public JwtPrincipal requireTeacher(String authorizationHeader) {
        JwtPrincipal principal = requirePrincipal(authorizationHeader);
        if (!principal.isTeacher()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers can access this endpoint");
        }
        return principal;
    }

    public JwtPrincipal requireStudent(String authorizationHeader) {
        JwtPrincipal principal = requirePrincipal(authorizationHeader);
        if (!principal.isStudent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can access this endpoint");
        }
        return principal;
    }
}
