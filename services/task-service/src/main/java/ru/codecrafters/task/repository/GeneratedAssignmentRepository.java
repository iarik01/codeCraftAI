package ru.codecrafters.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.GeneratedAssignmentEntity;

public interface GeneratedAssignmentRepository extends JpaRepository<GeneratedAssignmentEntity, UUID> {
    List<GeneratedAssignmentEntity> findAllByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    Optional<GeneratedAssignmentEntity> findByIdAndTeacherId(UUID id, UUID teacherId);
}
