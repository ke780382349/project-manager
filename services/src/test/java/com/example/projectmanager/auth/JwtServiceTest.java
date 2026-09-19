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
        String userId = "A1B2C3D4E5F678901234567890ABCDEF";
        setIdForTest(user, userId);

        String token = jwtService.createToken(AuthenticatedUser.from(user));

        assertEquals(userId, jwtService.getUserId(token));
    }

    @Test
    void rejectsSecretShorterThanThirtyTwoBytes() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("too-short", Duration.ofDays(30)));
    }

    private static void setIdForTest(User user, String id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
