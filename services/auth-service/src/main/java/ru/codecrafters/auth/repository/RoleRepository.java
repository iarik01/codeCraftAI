package ru.codecrafters.auth.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.auth.domain.Role;
import ru.codecrafters.auth.domain.RoleCode;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(RoleCode code);
}
