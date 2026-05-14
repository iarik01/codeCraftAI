package ru.codecrafters.task.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.codecrafters.task.domain.GroupMemberEntity;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, UUID> {
    boolean existsByGroupIdAndStudentId(UUID groupId, UUID studentId);

    List<GroupMemberEntity> findAllByGroupIdOrderByJoinedAtDesc(UUID groupId);

    List<GroupMemberEntity> findAllByStudentId(UUID studentId);
}
