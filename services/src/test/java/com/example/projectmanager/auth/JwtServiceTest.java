package com.example.projectmanager.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.projectmanager.user.User;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-longer-than-32-characters";

    @Test
    void createsTokenThatContainsTheUserId() {
        JwtService jwtService = new JwtService(SECRET, Duration.ofDays(30));
        User user = new User("test@example.com", "hashed-password", "Test User");
        setIdForTest(user, 42L);

        String token = jwtService.createToken(AuthenticatedUser.from(user));

        assertEquals(42L, jwtService.getUserId(token));
    }

    @Test
    void rejectsSecretShorterThanThirtyTwoBytes() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("too-short", Duration.ofDays(30)));
    }

    private static void setIdForTest(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
