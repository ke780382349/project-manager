package com.example.projectmanager.task;

import com.example.projectmanager.auth.AuthenticatedUser;
import com.example.projectmanager.user.PermissionIds;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.TASK_VIEW + "')")
    public List<TaskResponse> listTasks(@RequestParam(name = "projectId", required = false) String projectId,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        return taskService.listTasks(projectId, user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.TASK_VIEW + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return taskService.createTask(request, user);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.TASK_MANAGE + "')")
    public TaskResponse updateTask(@PathVariable("id") String id,
                                   @Valid @RequestBody UpdateTaskRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return taskService.updateTask(id, request, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.TASK_MANAGE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("id") String id, @AuthenticationPrincipal AuthenticatedUser user) {
        taskService.deleteTask(id, user);
    }
}
