package com.example.projectmanager.auth;

import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该邮箱已经注册");
        }
        User user = new User(email, passwordEncoder.encode(request.password()), request.displayName().trim());
        return userRepository.save(user);
    }

    public User authenticate(LoginRequest request) {
        String account = request.account().trim();
        User user = findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "邮箱或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "邮箱或密码错误");
        }
        return user;
    }

    private java.util.Optional<User> findByAccount(String account) {
        if (account.contains("@")) {
            return userRepository.findByEmailIgnoreCase(normalizeEmail(account));
        }
        return userRepository.findByUsernameIgnoreCase(account);
    }

    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.invalidateTokens();
            userRepository.save(user);
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
