package ru.codecrafters.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.GroupEntity;

public interface GroupRepository extends JpaRepository<GroupEntity, UUID> {
    List<GroupEntity> findAllByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    Optional<GroupEntity> findByIdAndTeacherId(UUID id, UUID teacherId);

    Optional<GroupEntity> findByInviteCodeIgnoreCase(String inviteCode);
}
