package ru.codecrafters.task.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.AssignmentGenerationRequestEntity;

public interface AssignmentGenerationRequestRepository extends JpaRepository<AssignmentGenerationRequestEntity, UUID> {
}
