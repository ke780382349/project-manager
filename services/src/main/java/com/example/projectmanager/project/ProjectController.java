package com.example.projectmanager.project;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.PROJECT_VIEW + "')")
    public List<ProjectResponse> listProjects(@AuthenticationPrincipal AuthenticatedUser user) {
        return projectService.listProjects(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.PROJECT_VIEW + "')")
    public ProjectResponse getProject(@PathVariable("id") String id, @AuthenticationPrincipal AuthenticatedUser user) {
        return projectService.getProject(id, user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.PROJECT_MANAGE + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request,
                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return projectService.createProject(request, user);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.PROJECT_MANAGE + "')")
    public ProjectResponse updateProject(@PathVariable("id") String id,
                                         @Valid @RequestBody UpdateProjectRequest request,
                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return projectService.updateProject(id, request, user);
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('" + PermissionIds.PROJECT_MANAGE + "')")
    public ProjectResponse addMember(@PathVariable("id") String id,
                                     @Valid @RequestBody AddProjectMemberRequest request,
                                     @AuthenticationPrincipal AuthenticatedUser user) {
        return projectService.addMember(id, request, user);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('" + PermissionIds.PROJECT_MANAGE + "')")
    public ProjectResponse removeMember(@PathVariable("id") String id,
                                        @PathVariable("userId") String userId,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        return projectService.removeMember(id, userId, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.PROJECT_MANAGE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable("id") String id, @AuthenticationPrincipal AuthenticatedUser user) {
        projectService.deleteProject(id, user);
    }
}
