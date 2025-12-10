package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTests {
    @Test
    @DisplayName("Constructor with Name, Email, Password works")
    void testConstructorWithName() {
        User user = new User("Name", "email@example.com", "password123");

        assertEquals("Name", user.getName());
        assertEquals("email@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    @Test
    @DisplayName("Constructor with Email, Password works")
    void testConstructorWithEmail() {
        User user = new User("email@example.com", "password123");
        assertEquals("", user.getName());
        assertEquals("email@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    @Test
    @DisplayName("Default constructor works")
    void testDefaultConstructor() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("Setters, Getters, and Lifecycle methods work")
    void testSettersGetters() {
        User user = new User();
        UUID generatedId = UUID.randomUUID();

        user.setId(generatedId);
        user.setName("Set Name");
        user.setEmail("Set Email");
        user.setPassword("Set Password");
        user.setBio("Bio");
        user.setProfilePhoto("photo.jpg");
        user.setPreferences("Prefs");
        user.setFavoriteWorkoutType("RUNNING");
        user.setWeeklyDurationGoal(120);
        user.setDailyCalorieGoal(2000);

        // Simulate lifecycle
        user.onCreate();
        user.onUpdate();

        assertEquals(generatedId, user.getId());
        assertEquals("Set Name", user.getName());
        assertEquals("Set Email", user.getEmail());
        assertEquals("Set Password", user.getPassword());
        assertEquals("Bio", user.getBio());
        assertEquals("photo.jpg", user.getProfilePhoto());
        assertEquals("Prefs", user.getPreferences());
        assertEquals("RUNNING", user.getFavoriteWorkoutType());
        assertEquals(120, user.getWeeklyDurationGoal());
        assertEquals(2000, user.getDailyCalorieGoal());

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }
}