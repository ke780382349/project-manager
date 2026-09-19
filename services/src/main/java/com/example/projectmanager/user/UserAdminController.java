package com.example.projectmanager.user;

import com.example.projectmanager.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('" + PermissionIds.USER_MANAGE + "')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userAdminService.listUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userAdminService.createUser(request);
    }

    @PatchMapping("/{userId}/status")
    public UserResponse updateStatus(
            @AuthenticationPrincipal AuthenticatedUser operator,
            @PathVariable("userId") String userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return userAdminService.updateStatus(operator.getId(), userId, request.enabled());
    }

    @PatchMapping("/{userId}/role")
    public UserResponse updateRole(
            @AuthenticationPrincipal AuthenticatedUser operator,
            @PathVariable("userId") String userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return userAdminService.updateRole(operator.getId(), userId, request.roleId());
    }
}
