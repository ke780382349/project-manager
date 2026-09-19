package com.example.projectmanager.auth;

import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public void registerDisabled() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "系统不开放自助注册，请联系管理员开户");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request);
        AuthenticatedUser authenticatedUser = authService.toAuthenticatedUser(user);
        String token = jwtService.createToken(authenticatedUser);
        return new AuthResponse(token, UserResponse.from(authenticatedUser));
    }

    @GetMapping("/me")
    public UserResponse currentUser(@AuthenticationPrincipal AuthenticatedUser user) {
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@AuthenticationPrincipal AuthenticatedUser user) {
        authService.logout(user.getId());
        return Map.of("message", "已退出登录");
    }
}
