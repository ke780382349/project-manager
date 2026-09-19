package com.example.projectmanager.task;

import com.example.projectmanager.auth.AuthenticatedUser;
import com.example.projectmanager.project.Project;
import com.example.projectmanager.project.ProjectResponse;
import com.example.projectmanager.project.ProjectService;
import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectService projectService, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(String projectId, AuthenticatedUser current) {
        List<String> projectIds;
        if (projectId == null || projectId.isBlank()) {
            projectIds = projectService.listProjects(current).stream().map(ProjectResponse::id).toList();
        } else {
            projectIds = List.of(projectService.requireVisibleProject(projectId.trim(), current).getId());
        }
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return toResponses(taskRepository.findByProjectIdInOrderByCreatedAtDesc(projectIds));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, AuthenticatedUser current) {
        Project project = projectService.requireVisibleProject(request.projectId().trim(), current);
        String assigneeId = cleanUserId(request.assigneeId());
        if (assigneeId != null) {
            requireUser(assigneeId);
        }
        Task task = new Task(
                project.getId(),
                request.title().trim(),
                cleanDescription(request.description()),
                assigneeId,
                current.getId()
        );
        return toResponses(List.of(taskRepository.save(task))).get(0);
    }

    @Transactional
    public TaskResponse updateTask(String id, UpdateTaskRequest request, AuthenticatedUser current) {
        Task task = requireExisting(id);
        projectService.requireVisibleProject(task.getProjectId(), current);
        String assigneeId = cleanUserId(request.assigneeId());
        if (assigneeId != null) {
            requireUser(assigneeId);
        }
        task.setTitle(request.title().trim());
        task.setDescription(cleanDescription(request.description()));
        task.setStatus(request.status());
        task.setAssigneeId(assigneeId);
        return toResponses(List.of(taskRepository.save(task))).get(0);
    }

    @Transactional
    public void deleteTask(String id, AuthenticatedUser current) {
        Task task = requireExisting(id);
        projectService.requireVisibleProject(task.getProjectId(), current);
        taskRepository.delete(task);
    }

    private Task requireExisting(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "任务不存在"));
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "负责人不存在"));
    }

    private List<TaskResponse> toResponses(List<Task> tasks) {
        Set<String> userIds = new HashSet<>();
        tasks.forEach(task -> {
            userIds.add(task.getCreatorId());
            if (task.getAssigneeId() != null) {
                userIds.add(task.getAssigneeId());
            }
        });
        Map<String, String> names = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));
        return tasks.stream()
                .map(task -> new TaskResponse(
                        task.getId(),
                        task.getProjectId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getAssigneeId(),
                        task.getAssigneeId() == null ? null : names.getOrDefault(task.getAssigneeId(), "已注销用户"),
                        task.getCreatorId(),
                        names.getOrDefault(task.getCreatorId(), "已注销用户"),
                        task.getCreatedAt(),
                        task.getUpdatedAt()))
                .toList();
    }

    private String cleanUserId(String userId) {
        return userId == null || userId.isBlank() ? null : userId.trim();
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
