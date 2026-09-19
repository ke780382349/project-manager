package com.example.projectmanager.bug;

import com.example.projectmanager.auth.AuthenticatedUser;
import com.example.projectmanager.project.Project;
import com.example.projectmanager.project.ProjectResponse;
import com.example.projectmanager.project.ProjectService;
import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BugService {

    private final BugRepository bugRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    public BugService(BugRepository bugRepository, ProjectService projectService, UserRepository userRepository) {
        this.bugRepository = bugRepository;
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BugResponse> listBugs(String projectId, AuthenticatedUser current) {
        List<String> projectIds;
        if (projectId == null || projectId.isBlank()) {
            projectIds = projectService.listProjects(current).stream().map(ProjectResponse::id).toList();
        } else {
            projectIds = List.of(projectService.requireVisibleProject(projectId.trim(), current).getId());
        }
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return toResponses(bugRepository.findByProjectIdInOrderByCreatedAtDesc(projectIds));
    }

    @Transactional
    public BugResponse createBug(CreateBugRequest request, AuthenticatedUser current) {
        Project project = projectService.requireVisibleProject(request.projectId().trim(), current);
        Bug bug = new Bug(
                project.getId(),
                request.title().trim(),
                cleanDescription(request.description()),
                request.severity(),
                current.getId()
        );
        return toResponses(List.of(bugRepository.save(bug))).get(0);
    }

    @Transactional
    public BugResponse updateBug(String id, UpdateBugRequest request, AuthenticatedUser current) {
        Bug bug = requireExisting(id);
        projectService.requireVisibleProject(bug.getProjectId(), current);
        bug.setTitle(request.title().trim());
        bug.setDescription(cleanDescription(request.description()));
        bug.setSeverity(request.severity());
        bug.setStatus(request.status());
        return toResponses(List.of(bugRepository.save(bug))).get(0);
    }

    @Transactional
    public void deleteBug(String id, AuthenticatedUser current) {
        Bug bug = requireExisting(id);
        projectService.requireVisibleProject(bug.getProjectId(), current);
        bugRepository.delete(bug);
    }

    private Bug requireExisting(String id) {
        return bugRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bug 不存在"));
    }

    private List<BugResponse> toResponses(List<Bug> bugs) {
        Set<String> reporterIds = bugs.stream().map(Bug::getReporterId).collect(Collectors.toSet());
        Map<String, String> names = userRepository.findAllById(reporterIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));
        return bugs.stream()
                .map(bug -> new BugResponse(
                        bug.getId(),
                        bug.getProjectId(),
                        bug.getTitle(),
                        bug.getDescription(),
                        bug.getSeverity(),
                        bug.getStatus(),
                        bug.getReporterId(),
                        names.getOrDefault(bug.getReporterId(), "已注销用户"),
                        bug.getCreatedAt(),
                        bug.getUpdatedAt()))
                .toList();
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
