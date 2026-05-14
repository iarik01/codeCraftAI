package ru.codecrafters.task.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codecrafters.task.security.JwtPrincipal;
import ru.codecrafters.task.security.JwtPrincipalResolver;
import ru.codecrafters.task.service.GroupService;
import ru.codecrafters.task.web.dto.AddStudentRequest;
import ru.codecrafters.task.web.dto.CreateGroupRequest;
import ru.codecrafters.task.web.dto.GroupResponse;
import ru.codecrafters.task.web.dto.StudentResponse;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;
    private final JwtPrincipalResolver principalResolver;

    public GroupController(GroupService groupService, JwtPrincipalResolver principalResolver) {
        this.groupService = groupService;
        this.principalResolver = principalResolver;
    }

    @PostMapping
    public GroupResponse create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return groupService.create(teacher.userId(), request);
    }

    @GetMapping
    public List<GroupResponse> findAll(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return groupService.findAll(teacher.userId());
    }

    @GetMapping("/{groupId}")
    public GroupResponse findById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID groupId
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return groupService.findById(teacher.userId(), groupId);
    }

    @PostMapping("/{groupId}/students")
    public StudentResponse addStudent(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID groupId,
            @Valid @RequestBody AddStudentRequest request
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return groupService.addStudent(teacher.userId(), groupId, request);
    }

    @GetMapping("/{groupId}/students")
    public List<StudentResponse> findStudents(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID groupId
    ) {
        JwtPrincipal teacher = principalResolver.requireTeacher(authorization);
        return groupService.findStudents(teacher.userId(), groupId);
    }
}
