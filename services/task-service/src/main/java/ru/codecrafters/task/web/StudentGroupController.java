package ru.codecrafters.task.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codecrafters.task.security.JwtPrincipal;
import ru.codecrafters.task.security.JwtPrincipalResolver;
import ru.codecrafters.task.service.GroupService;
import ru.codecrafters.task.web.dto.GroupResponse;
import ru.codecrafters.task.web.dto.JoinGroupRequest;

@RestController
@RequestMapping("/api/student/groups")
public class StudentGroupController {
    private final GroupService groupService;
    private final JwtPrincipalResolver principalResolver;

    public StudentGroupController(GroupService groupService, JwtPrincipalResolver principalResolver) {
        this.groupService = groupService;
        this.principalResolver = principalResolver;
    }

    @PostMapping("/join")
    public GroupResponse join(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody JoinGroupRequest request
    ) {
        JwtPrincipal student = principalResolver.requireStudent(authorization);
        return groupService.joinByInviteCode(student.userId(), request);
    }

    @GetMapping
    public List<GroupResponse> findMyGroups(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        JwtPrincipal student = principalResolver.requireStudent(authorization);
        return groupService.findStudentGroups(student.userId());
    }
}
