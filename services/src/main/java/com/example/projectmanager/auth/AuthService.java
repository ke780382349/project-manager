package com.example.projectmanager.auth;

import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserRepository;
import com.example.projectmanager.user.Role;
import com.example.projectmanager.user.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(LoginRequest request) {
        String account = request.account().trim();
        User user = findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "邮箱或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "邮箱或密码错误");
        }
        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "账号已停用，请联系管理员");
        }
        return user;
    }

    public AuthenticatedUser toAuthenticatedUser(User user) {
        Role role = user.getRoleId() == null ? null : roleRepository.findById(user.getRoleId()).orElse(null);
        if (role == null) throw new IllegalStateException("用户角色不存在: " + user.getRoleId());
        return AuthenticatedUser.from(user, role);
    }

    private java.util.Optional<User> findByAccount(String account) {
        if (account.contains("@")) {
            return userRepository.findByEmailIgnoreCase(normalizeEmail(account));
        }
        return userRepository.findByUsernameIgnoreCase(account);
    }

    @Transactional
    public void logout(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.invalidateTokens();
            userRepository.save(user);
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
