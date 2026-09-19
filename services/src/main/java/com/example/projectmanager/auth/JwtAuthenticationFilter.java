package com.example.projectmanager.auth;

import com.example.projectmanager.user.User;
import com.example.projectmanager.user.UserRepository;
import com.example.projectmanager.user.RoleRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, RoleRepository roleRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String userId = jwtService.getUserId(token);
                Optional<User> user = userRepository.findById(userId);
                user.filter(currentUser -> currentUser.isEnabled()
                                && currentUser.getTokenVersion() == jwtService.getTokenVersion(token))
                        .flatMap(currentUser -> currentUser.getRoleId() == null
                                ? Optional.empty()
                                : roleRepository.findById(currentUser.getRoleId())
                                        .map(role -> AuthenticatedUser.from(currentUser, role)))
                        .ifPresent(authenticatedUser -> {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            authenticatedUser,
                                            null,
                                            authenticatedUser.getAuthorities()
                                    );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            } catch (JwtException | IllegalArgumentException ignored) {
                // 无效令牌交给 Spring Security 处理，不让它影响公开接口。
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
