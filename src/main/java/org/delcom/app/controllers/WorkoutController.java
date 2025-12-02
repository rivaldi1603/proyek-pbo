package org.delcom.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Workout;
import org.delcom.app.entities.User;
import org.delcom.app.services.WorkoutService;
import org.delcom.app.dto.WorkoutForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;

    @Autowired
    protected AuthContext authContext;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    // Menambahkan workout baru
    // -------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, UUID>>> createWorkout(@RequestBody WorkoutForm reqWorkout) {

        if (reqWorkout.getTitle() == null || reqWorkout.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data title tidak valid", null));
        } else if (reqWorkout.getDescription() == null || reqWorkout.getDescription().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data description tidak valid", null));
        } else if (reqWorkout.getDurationMinutes() == null || reqWorkout.getDurationMinutes() <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Data durationMinutes tidak valid", null));
        } else if (reqWorkout.getType() == null || reqWorkout.getType().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data type tidak valid", null));
        } else if (reqWorkout.getDate() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data date tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Workout newWorkout = workoutService.createWorkout(authUser.getId(), reqWorkout.getTitle(),
                reqWorkout.getDescription(), reqWorkout.getDurationMinutes(), reqWorkout.getType(),
                reqWorkout.getDate());
        return ResponseEntity.ok(new ApiResponse<Map<String, UUID>>(
                "success",
                "Workout berhasil dibuat",
                Map.of("id", newWorkout.getId())));
    }

    // Mendapatkan semua workout dengan opsi pencarian
    // -------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Workout>>>> getAllWorkouts(
            @RequestParam(required = false) String search) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<Workout> workouts = workoutService.getAllWorkouts(authUser.getId(), search, null);
        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Daftar workout berhasil diambil",
                Map.of("workouts", workouts)));
    }

    // Mendapatkan workout berdasarkan ID
    // -------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Workout>>> getWorkoutById(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Workout workout = workoutService.getWorkoutById(authUser.getId(), id);
        if (workout == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data workout tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data workout berhasil diambil",
                Map.of("workout", workout)));
    }

    // Memperbarui workout berdasarkan ID
    // -------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Workout>> updateWorkout(@PathVariable UUID id,
            @RequestBody WorkoutForm reqWorkout) {

        if (reqWorkout.getTitle() == null || reqWorkout.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data title tidak valid", null));
        } else if (reqWorkout.getDescription() == null || reqWorkout.getDescription().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data description tidak valid", null));
        } else if (reqWorkout.getDurationMinutes() == null || reqWorkout.getDurationMinutes() <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Data durationMinutes tidak valid", null));
        } else if (reqWorkout.getType() == null || reqWorkout.getType().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data type tidak valid", null));
        } else if (reqWorkout.getDate() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data date tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Workout updatedWorkout = workoutService.updateWorkout(authUser.getId(), id, reqWorkout.getTitle(),
                reqWorkout.getDescription(), reqWorkout.getDurationMinutes(), reqWorkout.getType(),
                reqWorkout.getDate());
        if (updatedWorkout == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data workout tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>("success", "Data workout berhasil diperbarui", null));
    }

    // Menghapus workout berdasarkan ID
    // -------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWorkout(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        boolean status = workoutService.deleteWorkout(authUser.getId(), id);
        if (!status) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data workout tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data workout berhasil dihapus",
                null));
    }
}
