package ru.codecrafters.task.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.codecrafters.task.domain.GroupEntity;
import ru.codecrafters.task.domain.GroupMemberEntity;
import ru.codecrafters.task.domain.UserAccountEntity;
import ru.codecrafters.task.repository.GroupMemberRepository;
import ru.codecrafters.task.repository.GroupRepository;
import ru.codecrafters.task.repository.UserAccountRepository;
import ru.codecrafters.task.web.dto.AddStudentRequest;
import ru.codecrafters.task.web.dto.CreateGroupRequest;
import ru.codecrafters.task.web.dto.GroupResponse;
import ru.codecrafters.task.web.dto.JoinGroupRequest;
import ru.codecrafters.task.web.dto.StudentResponse;

@Service
public class GroupService {
    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();
    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserAccountRepository userRepository;

    public GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            UserAccountRepository userRepository
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public GroupResponse create(UUID teacherId, CreateGroupRequest request) {
        GroupEntity group = new GroupEntity(
                teacherId,
                request.name().trim(),
                normalizeNullable(request.description()),
                generateInviteCode()
        );
        return GroupResponse.from(groupRepository.save(group));
    }

    public List<GroupResponse> findAll(UUID teacherId) {
        return groupRepository.findAllByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(GroupResponse::from)
                .toList();
    }

    public GroupResponse findById(UUID teacherId, UUID groupId) {
        return GroupResponse.from(requireTeacherGroup(teacherId, groupId));
    }

    public StudentResponse addStudent(UUID teacherId, UUID groupId, AddStudentRequest request) {
        GroupEntity group = requireTeacherGroup(teacherId, groupId);
        UserAccountEntity student = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student was not found"));

        if (!"STUDENT".equals(student.getRole().getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only users with STUDENT role can be added to groups");
        }

        if (memberRepository.existsByGroupIdAndStudentId(group.getId(), student.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is already in this group");
        }

        GroupMemberEntity membership = memberRepository.save(new GroupMemberEntity(group.getId(), student.getId()));
        return StudentResponse.from(student, membership);
    }

    public GroupResponse joinByInviteCode(UUID studentId, JoinGroupRequest request) {
        GroupEntity group = groupRepository.findByInviteCodeIgnoreCase(request.inviteCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found by invite code"));

        if (memberRepository.existsByGroupIdAndStudentId(group.getId(), studentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already a member of this group");
        }

        memberRepository.save(new GroupMemberEntity(group.getId(), studentId));
        return GroupResponse.from(group);
    }

    public List<GroupResponse> findStudentGroups(UUID studentId) {
        List<UUID> groupIds = memberRepository.findAllByStudentId(studentId).stream()
                .map(GroupMemberEntity::getGroupId)
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return groupRepository.findAllById(groupIds).stream()
                .map(GroupResponse::from)
                .toList();
    }

    public List<StudentResponse> findStudents(UUID teacherId, UUID groupId) {
        requireTeacherGroup(teacherId, groupId);
        List<GroupMemberEntity> memberships = memberRepository.findAllByGroupIdOrderByJoinedAtDesc(groupId);
        Map<UUID, UserAccountEntity> studentsById = userRepository.findAllById(
                        memberships.stream().map(GroupMemberEntity::getStudentId).toList()
                ).stream()
                .collect(Collectors.toMap(UserAccountEntity::getId, Function.identity()));

        return memberships.stream()
                .map(membership -> StudentResponse.from(studentsById.get(membership.getStudentId()), membership))
                .toList();
    }

    private GroupEntity requireTeacherGroup(UUID teacherId, UUID groupId) {
        return groupRepository.findByIdAndTeacherId(groupId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
        for (int index = 0; index < INVITE_CODE_LENGTH; index++) {
            code.append(INVITE_ALPHABET.charAt(random.nextInt(INVITE_ALPHABET.length())));
        }
        return code.toString().toUpperCase(Locale.ROOT);
    }
}
