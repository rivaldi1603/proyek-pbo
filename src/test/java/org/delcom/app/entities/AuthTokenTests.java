package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthTokenTests {
    @Test
    @DisplayName("Constructor with arguments sets fields correctly")
    void testConstructorWithArgs() {
        UUID userId = UUID.randomUUID();
        String token = "token123";
        AuthToken authToken = new AuthToken(userId, token);

        assertEquals(token, authToken.getToken());
        assertEquals(userId, authToken.getUserId());
        assertNotNull(authToken.getCreatedAt());
    }

    @Test
    @DisplayName("Default constructor creates empty object")
    void testDefaultConstructor() {
        AuthToken authToken = new AuthToken();

        assertNull(authToken.getId());
        assertNull(authToken.getToken());
        assertNull(authToken.getUserId());
    }

    @Test
    @DisplayName("Setters and Getters work correctly")
    void testSettersAndGetters() {
        AuthToken authToken = new AuthToken();
        UUID generatedId = UUID.randomUUID();
        UUID generatedUserId = UUID.randomUUID();

        authToken.setId(generatedId);
        authToken.setUserId(generatedUserId);
        authToken.setToken("Set Token");

        // Simulate PrePersist
        authToken.onCreate();

        assertEquals(generatedId, authToken.getId());
        assertEquals(generatedUserId, authToken.getUserId());
        assertEquals("Set Token", authToken.getToken());
        assertNotNull(authToken.getCreatedAt());
    }
}