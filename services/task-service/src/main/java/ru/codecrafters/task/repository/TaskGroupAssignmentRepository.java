package ru.codecrafters.task.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.TaskGroupAssignmentEntity;

public interface TaskGroupAssignmentRepository extends JpaRepository<TaskGroupAssignmentEntity, UUID> {
    boolean existsByTaskIdAndGroupId(UUID taskId, UUID groupId);

    boolean existsByTaskIdAndGroupIdIn(UUID taskId, Collection<UUID> groupIds);

    List<TaskGroupAssignmentEntity> findAllByTaskId(UUID taskId);

    List<TaskGroupAssignmentEntity> findAllByTaskIdIn(Collection<UUID> taskIds);

    List<TaskGroupAssignmentEntity> findAllByGroupIdIn(Collection<UUID> groupIds);

    Optional<TaskGroupAssignmentEntity> findByTaskIdAndGroupId(UUID taskId, UUID groupId);
}
