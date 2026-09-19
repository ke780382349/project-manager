package com.example.projectmanager.project;

import com.example.projectmanager.auth.AuthenticatedUser;
import com.example.projectmanager.user.Permission;
import com.example.projectmanager.user.PermissionIds;
import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(AuthenticatedUser current) {
        List<Project> projects = canManage(current)
                ? projectRepository.findAllByOrderByUpdatedAtDesc()
                : projectRepository.findVisibleFor(current.getId());
        return projects.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(String id, AuthenticatedUser current) {
        return toResponse(resolveVisible(id, current));
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, AuthenticatedUser current) {
        Project project = new Project(
                request.name().trim(),
                cleanDescription(request.description()),
                current.getId()
        );
        project.setMembers(resolveMembers(Set.of(), current.getId()));
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(String id, UpdateProjectRequest request, AuthenticatedUser current) {
        Project project = resolveVisible(id, current);
        project.setName(request.name().trim());
        project.setDescription(cleanDescription(request.description()));
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse addMember(String id, AddProjectMemberRequest request, AuthenticatedUser current) {
        Project project = resolveVisible(id, current);
        String userId = request.userId().trim();
        boolean alreadyMember = project.getMembers().stream().anyMatch(member -> member.getId().equals(userId));
        if (alreadyMember) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该成员已在项目中");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "要添加的用户不存在"));
        Set<User> members = new HashSet<>(project.getMembers());
        members.add(user);
        project.setMembers(members);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse removeMember(String id, String userId, AuthenticatedUser current) {
        Project project = resolveVisible(id, current);
        if (project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "负责人不能从成员中移除");
        }
        Set<User> members = new HashSet<>(project.getMembers());
        boolean removed = members.removeIf(member -> member.getId().equals(userId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "该用户不在项目成员中");
        }
        project.setMembers(members);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(String id, AuthenticatedUser current) {
        projectRepository.delete(resolveVisible(id, current));
    }

    private Project resolveVisible(String id, AuthenticatedUser current) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在"));
        boolean visible = canManage(current)
                || project.getOwnerId().equals(current.getId())
                || project.getMembers().stream().anyMatch(member -> member.getId().equals(current.getId()));
        if (!visible) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "没有访问该项目的权限");
        }
        return project;
    }

    private boolean canManage(AuthenticatedUser current) {
        return current.getPermissions().stream()
                .map(Permission::getId)
                .anyMatch(PermissionIds.PROJECT_MANAGE::equals);
    }

    private Set<User> resolveMembers(Set<String> memberIds, String ownerId) {
        Set<String> ids = new LinkedHashSet<>();
        ids.add(ownerId);
        if (memberIds != null) {
            for (String memberId : memberIds) {
                if (memberId != null && !memberId.isBlank()) {
                    ids.add(memberId.trim());
                }
            }
        }
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "成员中包含不存在的用户");
        }
        return new HashSet<>(users);
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    private ProjectResponse toResponse(Project project) {
        List<ProjectMemberResponse> members = project.getMembers().stream()
                .map(member -> ProjectMemberResponse.from(member, project.getOwnerId()))
                .sorted(Comparator.comparing(ProjectMemberResponse::id))
                .toList();
        ProjectMemberResponse owner = members.stream()
                .filter(ProjectMemberResponse::owner)
                .findFirst()
                .orElse(null);
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                owner,
                members,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
