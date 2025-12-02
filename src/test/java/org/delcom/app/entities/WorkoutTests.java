package org.delcom.app.entities;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.delcom.app.enums.WorkoutType;

public class WorkoutTests {
    @Test
    @DisplayName("Memembuat instance dari kelas Workout")
    void testMembuatInstanceWorkout() throws Exception {
        UUID userId = UUID.randomUUID();

        // Workout dengan constructor lengkap
        {
            LocalDate date = LocalDate.now();
            Workout workout = new Workout(userId, "Testing Title", "Testing Description", 60, 500.0, date,
                    WorkoutType.RUNNING, "/image.png");

            assert (workout.getUserId().equals(userId));
            assert (workout.getTitle().equals("Testing Title"));
            assert (workout.getDescription().equals("Testing Description"));
            assert (workout.getDurationMinutes() == 60);
            assert (workout.getCaloriesBurned() == 500.0);
            assert (workout.getDate().equals(date));
            assert (workout.getImagePath().equals("/image.png"));
        }

        // Workout dengan setNilai
        {
            Workout workout = new Workout();
            UUID generatedId = UUID.randomUUID();
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
            workout.onCreate();
            workout.onUpdate();

            assert (workout.getId().equals(generatedId));
            assert (workout.getUserId().equals(userId));
            assert (workout.getTitle().equals("Set Title"));
            assert (workout.getDescription().equals("Set Description"));
            assert (workout.getDurationMinutes() == 45);
            assert (workout.getCaloriesBurned() == 300.0);
            assert (workout.getType() == WorkoutType.RUNNING);
            assert (workout.getDate().equals(date));
            assert (workout.getImagePath().equals("/cover.png"));
            assert (workout.getCreatedAt() != null);
            assert (workout.getUpdatedAt() != null);
        }
    }
}