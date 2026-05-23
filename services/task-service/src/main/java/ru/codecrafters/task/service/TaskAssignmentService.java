package ru.codecrafters.task.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.codecrafters.task.domain.GeneratedTaskEntity;
import ru.codecrafters.task.domain.TaskGroupAssignmentEntity;
import ru.codecrafters.task.repository.GeneratedTaskRepository;
import ru.codecrafters.task.repository.GroupRepository;
import ru.codecrafters.task.repository.TaskGroupAssignmentRepository;
import ru.codecrafters.task.web.dto.AssignGroupsRequest;
import ru.codecrafters.task.web.dto.TaskGroupAssignmentResponse;

@Service
public class TaskAssignmentService {
    private final GeneratedTaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final TaskGroupAssignmentRepository assignmentRepository;

    public TaskAssignmentService(
            GeneratedTaskRepository taskRepository,
            GroupRepository groupRepository,
            TaskGroupAssignmentRepository assignmentRepository
    ) {
        this.taskRepository = taskRepository;
        this.groupRepository = groupRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<TaskGroupAssignmentResponse> assignGroups(UUID teacherId, UUID taskId, AssignGroupsRequest request) {
        GeneratedTaskEntity task = taskRepository.findByIdAndTeacherId(taskId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!"GENERATED".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only generated tasks can be assigned");
        }

        List<UUID> uniqueGroupIds = new LinkedHashSet<>(request.groupIds()).stream().toList();
        for (UUID groupId : uniqueGroupIds) {
            groupRepository.findByIdAndTeacherId(groupId, teacherId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + groupId));

            if (!assignmentRepository.existsByTaskIdAndGroupId(taskId, groupId)) {
                assignmentRepository.save(new TaskGroupAssignmentEntity(taskId, groupId, request.deadline()));
            }
        }

        return assignmentRepository.findAllByTaskId(taskId).stream()
                .filter(assignment -> uniqueGroupIds.contains(assignment.getGroupId()))
                .map(TaskGroupAssignmentResponse::from)
                .toList();
    }
}
