package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.delcom.app.enums.WorkoutType;

class WorkoutTests {
    @Test
    @DisplayName("Constructor with all args works")
    void testConstructorAllArgs() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Workout workout = new Workout(userId, "Testing Title", "Testing Description", 60, 500.0, date,
                WorkoutType.RUNNING, "/image.png");

        assertEquals(userId, workout.getUserId());
        assertEquals("Testing Title", workout.getTitle());
        assertEquals("Testing Description", workout.getDescription());
        assertEquals(60, workout.getDurationMinutes());
        assertEquals(500.0, workout.getCaloriesBurned());
        assertEquals(date, workout.getDate());
        assertEquals(WorkoutType.RUNNING, workout.getType());
        assertEquals("/image.png", workout.getImagePath());
    }

    @Test
    @DisplayName("Setters, Getters, and Lifecycle methods work")
    void testSettersGetters() {
        Workout workout = new Workout();
        UUID generatedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        workout.setId(generatedId);
        workout.setUserId(userId);
        workout.setTitle("Set Title");
        workout.setDescription("Set Description");
        workout.setDurationMinutes(45);
        workout.setCaloriesBurned(300.0);
        workout.setType(WorkoutType.RUNNING);
        workout.setDate(date);
        workout.setImagePath("/cover.png");

        // Simulate lifecycle
        workout.onCreate();
        workout.onUpdate();

        assertEquals(generatedId, workout.getId());
        assertEquals(userId, workout.getUserId());
        assertEquals("Set Title", workout.getTitle());
        assertEquals("Set Description", workout.getDescription());
        assertEquals(45, workout.getDurationMinutes());
        assertEquals(300.0, workout.getCaloriesBurned());
        assertEquals(WorkoutType.RUNNING, workout.getType());
        assertEquals(date, workout.getDate());
        assertEquals("/cover.png", workout.getImagePath());

        assertNotNull(workout.getCreatedAt());
        assertNotNull(workout.getUpdatedAt());
    }
}