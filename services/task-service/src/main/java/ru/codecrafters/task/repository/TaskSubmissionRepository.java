package ru.codecrafters.task.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.TaskSubmissionEntity;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmissionEntity, UUID> {
    Optional<TaskSubmissionEntity> findByTaskIdAndStudentId(UUID taskId, UUID studentId);

    List<TaskSubmissionEntity> findAllByTaskId(UUID taskId);

    List<TaskSubmissionEntity> findAllByTaskIdInAndStudentId(Collection<UUID> taskIds, UUID studentId);
}
