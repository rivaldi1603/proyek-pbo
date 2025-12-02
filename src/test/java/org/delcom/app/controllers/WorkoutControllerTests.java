package org.delcom.app.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Workout;
import org.delcom.app.entities.User;
import org.delcom.app.enums.WorkoutType;
import org.delcom.app.services.WorkoutService;
import org.delcom.app.dto.WorkoutForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

public class WorkoutControllerTests {
    @Test
    @DisplayName("Pengujian untuk controller Workout")
    void testWorkoutController() throws Exception {
        // Buat random UUID
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID nonexistentWorkoutId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        // Membuat dummy data
        Workout workout = new Workout(userId, "Lari Pagi", "Lari keliling komplek", 30, 200.0, date,
                WorkoutType.RUNNING, null);
        workout.setId(workoutId);

        // Membuat mock ServiceRepository
        // Buat mock
        WorkoutService workoutService = Mockito.mock(WorkoutService.class);

        // Membuat instance controller
        WorkoutController workoutController = new WorkoutController(workoutService);
        assert (workoutController != null);

        workoutController.authContext = new AuthContext();
        User authUser = new User("Test User", "testuser@example.com");
        authUser.setId(userId);

        // Menguji method createWorkout
        {
            // Data tidak valid
            {
                List<WorkoutForm> invalidWorkouts = List.of(
                        // Title Null
                        createWorkoutForm(null, "Deskripsi valid", 30, "RUNNING", date),
                        // Calories Null
                        createWorkoutForm("Judul valid", "Deskripsi valid", 30, null, date),
                        // Calories Invalid (Type Empty)
                        createWorkoutForm("Judul valid", "Deskripsi valid", 30, "", date),
                        // Date Null
                        createWorkoutForm("Judul valid", "Deskripsi valid", 30, "RUNNING", null));

                ResponseEntity<ApiResponse<Map<String, UUID>>> result;
                for (WorkoutForm itemWorkout : invalidWorkouts) {
                    result = workoutController.createWorkout(itemWorkout);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Tidak terautentikasi untuk menambahkan workout
            {
                workoutController.authContext.setAuthUser(null);
                WorkoutForm form = createWorkoutForm("Title", "Desc", 30, "RUNNING", date);

                var result = workoutController.createWorkout(form);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Berhasil menambahkan workout
            {
                workoutController.authContext.setAuthUser(authUser);
                WorkoutForm form = createWorkoutForm("Title", "Desc", 30, "RUNNING", date);
                var result = workoutController.createWorkout(form);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // Menguji method getAllWorkouts
        {
            // Tidak terautentikasi untuk getAllWorkouts
            {
                workoutController.authContext.setAuthUser(null);

                var result = workoutController.getAllWorkouts(null);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Menguji getAllWorkouts dengan search null
            {
                workoutController.authContext.setAuthUser(authUser);

                List<Workout> dummyResponse = List.of(workout);
                when(workoutService.getAllWorkouts(any(UUID.class), any(String.class), any()))
                        .thenReturn(dummyResponse);
                var result = workoutController.getAllWorkouts(null);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // Menguji method getWorkoutById
        {
            // Tidak terautentikasi untuk getWorkoutById
            {
                workoutController.authContext.setAuthUser(null);

                var result = workoutController.getWorkoutById(workoutId);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            workoutController.authContext.setAuthUser(authUser);

            // Menguji getWorkoutById dengan ID yang ada
            {
                when(workoutService.getWorkoutById(any(UUID.class), any(UUID.class))).thenReturn(workout);
                var result = workoutController.getWorkoutById(workoutId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
                assert (result.getBody().getData().get("workout").getId().equals(workoutId));
            }

            // Menguji getWorkoutById dengan ID yang tidak ada
            {
                when(workoutService.getWorkoutById(any(UUID.class), any(UUID.class))).thenReturn(null);
                var result = workoutController.getWorkoutById(nonexistentWorkoutId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("fail"));
            }

        }

        // Menguji method updateWorkout
        {
            // Data tidak valid
            {
                List<WorkoutForm> invalidWorkouts = List.of(
                        // Title Null
                        createWorkoutForm(null, "Deskripsi valid", 30, "RUNNING", date),
                        // Title Kosong
                        createWorkoutForm("", "Deskripsi valid", 30, "RUNNING", date),
                        // Description Null
                        createWorkoutForm("Judul valid", null, 30, "RUNNING", date),
                        // Description Kosong
                        createWorkoutForm("Judul valid", "", 30, "RUNNING", date),
                        // Duration Null
                        createWorkoutForm("Judul valid", "Deskripsi valid", null, "RUNNING", date),
                        // Duration Invalid
                        createWorkoutForm("Judul valid", "Deskripsi valid", 0, "RUNNING", date),
                        // Type Null
                        createWorkoutForm("Judul valid", "Deskripsi valid", 30, null, date),
                        // Type Empty
                        createWorkoutForm("Judul valid", "Deskripsi valid", 30, "", date),
                        // Date Null
                        createWorkoutForm("Judul valid", "Deskripsi valid", 30, "RUNNING", null));

                for (WorkoutForm itemWorkout : invalidWorkouts) {
                    var result = workoutController.updateWorkout(workoutId, itemWorkout);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Tidak terautentikasi untuk updateWorkout
            {
                workoutController.authContext.setAuthUser(null);
                WorkoutForm form = createWorkoutForm("Title", "Desc", 30, "RUNNING", date);

                var result = workoutController.updateWorkout(workoutId, form);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            workoutController.authContext.setAuthUser(authUser);

            // Memperbarui workout dengan ID tidak ada
            {
                when(workoutService.updateWorkout(any(UUID.class), any(UUID.class), any(String.class),
                        any(String.class), any(Integer.class), any(String.class), any(LocalDate.class)))
                        .thenReturn(null);
                WorkoutForm form = createWorkoutForm("Updated Title", "Updated Desc", 45, "RUNNING", date);

                var result = workoutController.updateWorkout(nonexistentWorkoutId, form);
                assert (result != null);
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Memperbarui workout dengan ID ada
            {
                Workout updatedWorkout = new Workout(userId, "Updated Title", "Updated Desc", 45, 300.0, date,
                        WorkoutType.RUNNING, null);
                updatedWorkout.setId(workoutId);
                when(workoutService.updateWorkout(any(UUID.class), any(UUID.class), any(String.class),
                        any(String.class), any(Integer.class), any(String.class), any(LocalDate.class)))
                        .thenReturn(updatedWorkout);

                WorkoutForm form = createWorkoutForm("Updated Title", "Updated Desc", 45, "RUNNING", date);
                var result = workoutController.updateWorkout(workoutId, form);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // // Menguji method deleteWorkout
        {
            // Tidak terautentikasi untuk deleteWorkout
            {
                workoutController.authContext.setAuthUser(null);

                var result = workoutController.deleteWorkout(workoutId);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            workoutController.authContext.setAuthUser(authUser);

            // Menguji deleteWorkout dengan ID yang tidak ada
            {
                when(workoutService.deleteWorkout(any(UUID.class), any(UUID.class))).thenReturn(false);
                var result = workoutController.deleteWorkout(nonexistentWorkoutId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Menguji deleteWorkout dengan ID yang ada
            {
                when(workoutService.deleteWorkout(any(UUID.class), any(UUID.class))).thenReturn(true);
                var result = workoutController.deleteWorkout(workoutId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }
    }

    private WorkoutForm createWorkoutForm(String title, String description, Integer durationMinutes, String type,
            LocalDate date) {
        WorkoutForm form = new WorkoutForm();
        form.setTitle(title);
        form.setDescription(description);
        form.setDurationMinutes(durationMinutes);
        form.setType(type);
        form.setDate(date);
        return form;
    }
}
