package ru.codecrafters.task.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.AiGenerationRequestEntity;

public interface AiGenerationRequestRepository extends JpaRepository<AiGenerationRequestEntity, UUID> {
}
