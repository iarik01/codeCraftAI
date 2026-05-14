package ru.codecrafters.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.GeneratedTaskEntity;

public interface GeneratedTaskRepository extends JpaRepository<GeneratedTaskEntity, UUID> {
    List<GeneratedTaskEntity> findAllByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    Optional<GeneratedTaskEntity> findByIdAndTeacherId(UUID id, UUID teacherId);
}
