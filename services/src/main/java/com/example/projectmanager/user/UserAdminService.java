package com.example.projectmanager.user;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(user -> UserResponse.from(user, findRole(user)))
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该账号已经存在");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该邮箱已经存在");
        }
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色不存在"));
        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                role.getId()
        );
        return UserResponse.from(userRepository.save(user), role);
    }

    @Transactional
    public UserResponse updateStatus(String operatorId, String userId, boolean enabled) {
        User user = getUser(userId);
        if (operatorId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能停用或修改自己的账号状态");
        }
        user.setEnabled(enabled);
        return UserResponse.from(userRepository.save(user), findRole(user));
    }

    @Transactional
    public UserResponse updateRole(String operatorId, String userId, String roleId) {
        User user = getUser(userId);
        if (operatorId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能修改自己的角色");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色不存在"));
        user.setRoleId(role.getId());
        user.invalidateTokens();
        return UserResponse.from(userRepository.save(user), role);
    }

    private Role findRole(User user) {
        return user.getRoleId() == null ? null : roleRepository.findById(user.getRoleId()).orElse(null);
    }

    private User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }
}
