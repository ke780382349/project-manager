package com.example.projectmanager.requirement;

import com.example.projectmanager.auth.AuthenticatedUser;
import com.example.projectmanager.project.Project;
import com.example.projectmanager.project.ProjectResponse;
import com.example.projectmanager.project.ProjectService;
import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    public RequirementService(RequirementRepository requirementRepository,
                              ProjectService projectService,
                              UserRepository userRepository) {
        this.requirementRepository = requirementRepository;
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RequirementResponse> listRequirements(String projectId, AuthenticatedUser current) {
        Map<String, String> projectNames = new LinkedHashMap<>();
        List<String> projectIds;
        if (projectId == null || projectId.isBlank()) {
            for (ProjectResponse project : projectService.listProjects(current)) {
                projectNames.put(project.id(), project.name());
            }
            projectIds = new ArrayList<>(projectNames.keySet());
        } else {
            Project project = projectService.requireVisibleProject(projectId.trim(), current);
            projectNames.put(project.getId(), project.getName());
            projectIds = List.of(project.getId());
        }
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return toResponses(requirementRepository.findByProjectIdInOrderByCreatedAtDesc(projectIds), projectNames);
    }

    @Transactional
    public RequirementResponse createRequirement(CreateRequirementRequest request, AuthenticatedUser current) {
        Project project = projectService.requireVisibleProject(request.projectId().trim(), current);
        Requirement requirement = new Requirement(
                project.getId(),
                request.title().trim(),
                cleanDescription(request.description()),
                current.getId()
        );
        return toResponses(List.of(requirementRepository.save(requirement)), Map.of(project.getId(), project.getName())).get(0);
    }

    @Transactional
    public RequirementResponse updateRequirement(String id, UpdateRequirementRequest request, AuthenticatedUser current) {
        Requirement requirement = requireExisting(id);
        Project project = projectService.requireVisibleProject(requirement.getProjectId(), current);
        requirement.setTitle(request.title().trim());
        requirement.setDescription(cleanDescription(request.description()));
        requirement.setStatus(request.status());
        return toResponses(List.of(requirementRepository.save(requirement)), Map.of(project.getId(), project.getName())).get(0);
    }

    @Transactional
    public void deleteRequirement(String id, AuthenticatedUser current) {
        Requirement requirement = requireExisting(id);
        projectService.requireVisibleProject(requirement.getProjectId(), current);
        requirementRepository.delete(requirement);
    }

    private Requirement requireExisting(String id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在"));
    }

    private List<RequirementResponse> toResponses(List<Requirement> requirements, Map<String, String> projectNames) {
        Set<String> creatorIds = requirements.stream().map(Requirement::getCreatorId).collect(Collectors.toSet());
        Map<String, String> creatorNames = userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));
        return requirements.stream()
                .map(requirement -> new RequirementResponse(
                        requirement.getId(),
                        requirement.getProjectId(),
                        projectNames.get(requirement.getProjectId()),
                        requirement.getTitle(),
                        requirement.getDescription(),
                        requirement.getStatus(),
                        requirement.getCreatorId(),
                        creatorNames.getOrDefault(requirement.getCreatorId(), "已注销用户"),
                        requirement.getCreatedAt(),
                        requirement.getUpdatedAt()))
                .toList();
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
