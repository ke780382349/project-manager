package com.example.projectmanager.auth;

import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        String token = jwtService.createToken(AuthenticatedUser.from(user));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, UserResponse.from(user)));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request);
        String token = jwtService.createToken(AuthenticatedUser.from(user));
        return new AuthResponse(token, UserResponse.from(user));
    }

    @GetMapping("/me")
    public UserResponse currentUser(@AuthenticationPrincipal AuthenticatedUser user) {
        return new UserResponse(user.getId(), user.getUsernameValue(), user.getEmail(), user.getDisplayName(), user.getRole().name());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@AuthenticationPrincipal AuthenticatedUser user) {
        authService.logout(user.getId());
        return Map.of("message", "已退出登录");
    }
}
