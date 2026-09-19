package com.example.projectmanager.requirement;

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
@RequestMapping("/api/requirements")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.REQUIREMENT_VIEW + "')")
    public List<RequirementResponse> listRequirements(@RequestParam(name = "projectId", required = false) String projectId,
                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return requirementService.listRequirements(projectId, user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.REQUIREMENT_VIEW + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public RequirementResponse createRequirement(@Valid @RequestBody CreateRequirementRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return requirementService.createRequirement(request, user);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.REQUIREMENT_MANAGE + "')")
    public RequirementResponse updateRequirement(@PathVariable("id") String id,
                                                 @Valid @RequestBody UpdateRequirementRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return requirementService.updateRequirement(id, request, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.REQUIREMENT_MANAGE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRequirement(@PathVariable("id") String id, @AuthenticationPrincipal AuthenticatedUser user) {
        requirementService.deleteRequirement(id, user);
    }
}
