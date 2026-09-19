package com.example.projectmanager.bug;

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
@RequestMapping("/api/bugs")
public class BugController {

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.BUG_VIEW + "')")
    public List<BugResponse> listBugs(@RequestParam(name = "projectId", required = false) String projectId,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return bugService.listBugs(projectId, user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.BUG_VIEW + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public BugResponse createBug(@Valid @RequestBody CreateBugRequest request,
                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return bugService.createBug(request, user);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.BUG_MANAGE + "')")
    public BugResponse updateBug(@PathVariable("id") String id,
                                 @Valid @RequestBody UpdateBugRequest request,
                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return bugService.updateBug(id, request, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.BUG_MANAGE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBug(@PathVariable("id") String id, @AuthenticationPrincipal AuthenticatedUser user) {
        bugService.deleteBug(id, user);
    }
}
