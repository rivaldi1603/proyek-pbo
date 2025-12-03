package org.delcom.app.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.UUID;

import org.delcom.app.entities.Workout;
import org.delcom.app.enums.WorkoutType;
import org.delcom.app.repositories.WorkoutRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutServiceTests {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private WorkoutService workoutService;

    @Test
    void testCreateWorkout() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Workout workout = new Workout(userId, "Lari Pagi", "Lari keliling komplek", 30, 300.0, date,
                WorkoutType.RUNNING, null);

        lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

        Workout created = workoutService.createWorkout(userId, "Lari Pagi", "Lari keliling komplek", 30, "RUNNING",
                date);

        assertNotNull(created);
        assertEquals("Lari Pagi", created.getTitle());
        assertEquals(300.0, created.getCaloriesBurned());
        assertEquals(WorkoutType.RUNNING, created.getType());
    }

    @Test
    void testUpdateWorkout() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Workout existingWorkout = new Workout(userId, "Old", "Old Desc", 20, 200.0, date, WorkoutType.RUNNING, null);
        existingWorkout.setId(workoutId);

        lenient().when(workoutRepository.findByUserIdAndId(userId, workoutId))
                .thenReturn(java.util.Optional.of(existingWorkout));
        lenient().when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Workout updated = workoutService.updateWorkout(userId, workoutId, "Lari Sore", "Lari sore hari", 45, "RUNNING",
                date);

        assertNotNull(updated);
        assertEquals("Lari Sore", updated.getTitle());
        assertEquals(450.0, updated.getCaloriesBurned()); // 45 * 10
        assertEquals(WorkoutType.RUNNING, updated.getType());
    }

    @Test
    @DisplayName("Pengujian untuk service Workout")
    void testWorkoutService() throws Exception {
        // Buat random UUID
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID nonexistentWorkoutId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        // Membuat dummy data
        Workout workout = new Workout(userId, "Lari Pagi", "Lari keliling komplek", 30, 200.0, date,
                WorkoutType.RUNNING, null);
        workout.setId(workoutId);

        // Atur perilaku mock
        // Atur perilaku mock
        lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(workout);
        lenient().when(workoutRepository.findByKeyword(userId, "Lari")).thenReturn(java.util.List.of(workout));
        lenient().when(workoutRepository.findAllByUserId(userId)).thenReturn(java.util.List.of(workout));
        lenient().when(workoutRepository.findByUserIdAndId(userId, workoutId))
                .thenReturn(java.util.Optional.of(workout));
        lenient().when(workoutRepository.findByUserIdAndId(userId, nonexistentWorkoutId))
                .thenReturn(java.util.Optional.empty());
        lenient().when(workoutRepository.existsById(workoutId)).thenReturn(true);
        lenient().when(workoutRepository.existsById(nonexistentWorkoutId)).thenReturn(false);
        lenient().doNothing().when(workoutRepository).deleteById(any(UUID.class));

        // Buat mock untuk FileStorageService
        FileStorageService fileStorageService = Mockito.mock(FileStorageService.class);

        // Membuat instance service
        WorkoutService workoutService = new WorkoutService(workoutRepository, fileStorageService);
        assert (workoutService != null);

        // Menguji create workout
        {
            Workout createdWorkout = workoutService.createWorkout(userId, workout.getTitle(), workout.getDescription(),
                    workout.getDurationMinutes(), "RUNNING", workout.getDate());
            assert (createdWorkout != null);
            assert (createdWorkout.getId().equals(workoutId));
            assert (createdWorkout.getTitle().equals(workout.getTitle()));
            assert (createdWorkout.getDescription().equals(workout.getDescription()));
        }

        // Menguji getAllWorkouts
        {
            var workouts = workoutService.getAllWorkouts(userId, null, null);
            assert (workouts.size() == 1);
        }

        // Menguji getAllWorkouts dengan pencarian
        {
            var workouts = workoutService.getAllWorkouts(userId, "Lari", null);
            assert (workouts.size() == 1);

            workouts = workoutService.getAllWorkouts(userId, "     ", null);
            assert (workouts.size() == 1);
        }

        // Menguji getWorkoutById
        {

            Workout fetchedWorkout = workoutService.getWorkoutById(userId, workoutId);
            assert (fetchedWorkout != null);
            assert (fetchedWorkout.getId().equals(workoutId));
            assert (fetchedWorkout.getTitle().equals(workout.getTitle()));
            assert (fetchedWorkout.getDescription().equals(workout.getDescription()));
        }

        // Menguji getWorkoutById dengan ID yang tidak ada
        {
            Workout fetchedWorkout = workoutService.getWorkoutById(userId, nonexistentWorkoutId);
            assert (fetchedWorkout == null);
        }

        // Menguji updateWorkout
        {
            String updatedTitle = "Lari Sore";
            String updatedDescription = "Lari di taman kota";
            Integer updatedDuration = 45;
            String updatedType = "RUNNING";
            LocalDate updatedDate = LocalDate.now().plusDays(1);

            Workout updatedWorkout = workoutService.updateWorkout(userId, workoutId, updatedTitle, updatedDescription,
                    updatedDuration, updatedType, updatedDate);
            assert (updatedWorkout != null);
            assert (updatedWorkout.getTitle().equals(updatedTitle));
            assert (updatedWorkout.getDescription().equals(updatedDescription));
            assert (updatedWorkout.getDurationMinutes().equals(updatedDuration));
            assert (updatedWorkout.getCaloriesBurned().equals(450.0)); // 45 * 10
            assert (updatedWorkout.getDate().equals(updatedDate));
        }

        // Menguji update Workout dengan ID yang tidak ada
        {
            String updatedTitle = "Lari Sore";
            String updatedDescription = "Lari di taman kota";
            Integer updatedDuration = 45;
            String updatedType = "RUNNING";
            LocalDate updatedDate = LocalDate.now().plusDays(1);

            Workout updatedWorkout = workoutService.updateWorkout(userId, nonexistentWorkoutId, updatedTitle,
                    updatedDescription, updatedDuration, updatedType, updatedDate);
            assert (updatedWorkout == null);
        }

        // Menguji deleteWorkout
        {
            boolean deleted = workoutService.deleteWorkout(userId, workoutId);
            assert (deleted == true);
        }

        // Menguji updateWorkout milik user lain
        {
            Workout updated = workoutService.updateWorkout(UUID.randomUUID(), workoutId, "Judul", "Deskripsi", 60,
                    "RUNNING", date);
            assertNull(updated);
        }

        // Menguji deleteWorkout dengan image
        {
            Workout workoutWithImage = new Workout(userId, "With Image", "Desc", 30, 100.0, date, WorkoutType.RUNNING,
                    "image.jpg");
            UUID workoutWithImageId = UUID.randomUUID();
            workoutWithImage.setId(workoutWithImageId);

            lenient().when(workoutRepository.findByUserIdAndId(userId, workoutWithImageId))
                    .thenReturn(java.util.Optional.of(workoutWithImage));
            lenient().when(fileStorageService.deleteFile("image.jpg")).thenReturn(true);

            boolean deleted = workoutService.deleteWorkout(userId, workoutWithImageId);
            assert (deleted == true);
            Mockito.verify(fileStorageService).deleteFile("image.jpg");
        }

        // Menguji method updateImage dengan workout kosong
        {
            workoutId = UUID.randomUUID();
            lenient().when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.empty());
            Workout updatedWorkout = workoutService.updateImage(workoutId, "image1.png");
            assert (updatedWorkout == null);
        }

        // Menguji method updateImage dengan sebelumnya ada image
        {
            // Data
            String newImageFilename = "image2.png";

            // Mock
            lenient().when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.of(workout));
            lenient().when(fileStorageService.deleteFile("image1.png")).thenReturn(true);
            lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

            workout.setImagePath("image1.png");
            Workout updatedWorkout = workoutService.updateImage(workoutId, newImageFilename);
            assert (updatedWorkout != null);
            assert (updatedWorkout.getImagePath().equals(newImageFilename));
        }

        // Menguji method updateImage dengan sebelumnya belum ada image
        {
            // Data
            String newImageFilename = "image2.png";

            // Mock
            lenient().when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.of(workout));
            lenient().when(fileStorageService.deleteFile("image1.png")).thenReturn(true);
            lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

            workout.setImagePath(null);
            Workout updatedWorkout = workoutService.updateImage(workoutId, newImageFilename);
            assert (updatedWorkout != null);
            assert (updatedWorkout.getImagePath().equals(newImageFilename));
        }
    }

    @Test
    void testCreateWorkout_InvalidType_ShouldDefaultToRunning() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Workout workout = new Workout(userId, "Lari Pagi", "Lari keliling komplek", 30, 300.0, date,
                WorkoutType.RUNNING, null);

        lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

        Workout created = workoutService.createWorkout(userId, "Lari Pagi", "Lari keliling komplek", 30, "NGACAL",
                date);

        assertNotNull(created);
        assertEquals(WorkoutType.RUNNING, created.getType());
    }

    @Test
    void testCalculateCalories_AllTypes() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        // Map of Type -> Expected Multiplier
        java.util.Map<String, Double> typeMultipliers = java.util.Map.of(
                "RUNNING", 10.0,
                "CYCLING", 8.0,
                "GYM", 6.0,
                "BODYWEIGHT", 5.0,
                "PLANK", 4.0,
                "STRETCHING", 3.0);

        for (java.util.Map.Entry<String, Double> entry : typeMultipliers.entrySet()) {
            String typeStr = entry.getKey();
            Double multiplier = entry.getValue();
            Integer duration = 30;
            Double expectedCalories = duration * multiplier;

            Workout workout = new Workout(userId, "Title", "Desc", duration, expectedCalories, date,
                    WorkoutType.valueOf(typeStr), null);
            lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

            Workout created = workoutService.createWorkout(userId, "Title", "Desc", duration, typeStr, date);

            assertNotNull(created);
            assertEquals(expectedCalories, created.getCaloriesBurned(), "Calories mismatch for type: " + typeStr);
        }
    }

    @Test
    void testUpdateWorkout_InvalidType_ShouldDefaultToRunning() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Workout existingWorkout = new Workout(userId, "Old", "Old Desc", 20, 200.0, date, WorkoutType.RUNNING, null);
        existingWorkout.setId(workoutId);

        lenient().when(workoutRepository.findByUserIdAndId(userId, workoutId))
                .thenReturn(java.util.Optional.of(existingWorkout));
        lenient().when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Workout updated = workoutService.updateWorkout(userId, workoutId, "Lari Sore", "Lari sore hari", 45, "NGACAL",
                date);

        assertNotNull(updated);
        assertEquals(WorkoutType.RUNNING, updated.getType());
    }

    @Test
    void testGetAllWorkouts_InvalidType_ShouldReturnAll() {
        UUID userId = UUID.randomUUID();
        // Mock repository to return list when findAllByUserId is called (fallback)
        when(workoutRepository.findAllByUserId(userId)).thenReturn(java.util.List.of(new Workout()));

        // Call with invalid type
        var result = workoutService.getAllWorkouts(userId, null, "INVALID_TYPE");

        // Should catch exception and call findAllByUserId
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workoutRepository).findAllByUserId(userId);
    }

    @Test
    void testGetDashboardStats_WhenNoData_ShouldReturnZeros() {
        UUID userId = UUID.randomUUID();

        // Mock repository to return nulls
        when(workoutRepository.sumDurationByUserId(userId)).thenReturn(null);
        when(workoutRepository.sumCaloriesByUserId(userId)).thenReturn(null);
        when(workoutRepository.countByUserId(userId)).thenReturn(null);

        var stats = workoutService.getDashboardStats(userId);

        assertNotNull(stats);
        assertEquals(0, stats.get("totalDuration"));
        assertEquals(0.0, stats.get("totalCalories"));
        assertEquals(0, stats.get("totalWorkouts"));
    }

    @Test
    void testGetAllWorkouts_WithFilter() {
        UUID userId = UUID.randomUUID();
        String typeStr = "RUNNING";

        // Mock repository
        when(workoutRepository.findByUserIdAndType(any(UUID.class), any(WorkoutType.class), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        workoutService.getAllWorkouts(userId, null, typeStr);

        verify(workoutRepository).findByUserIdAndType(any(UUID.class), any(WorkoutType.class), any());
    }

    @Test
    void testGetAllWorkouts_NoFilter() {
        UUID userId = UUID.randomUUID();

        // Case 1: Null type
        workoutService.getAllWorkouts(userId, null, null);
        verify(workoutRepository).findAllByUserId(userId);

        // Case 2: Empty type
        workoutService.getAllWorkouts(userId, null, "");
        verify(workoutRepository, Mockito.times(2)).findAllByUserId(userId);
    }

    @Test
    void testDeleteWorkout_Success() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        Workout workout = new Workout();
        workout.setId(workoutId);

        when(workoutRepository.findByUserIdAndId(userId, workoutId)).thenReturn(java.util.Optional.of(workout));

        boolean result = workoutService.deleteWorkout(userId, workoutId);

        assertTrue(result);
        verify(workoutRepository).deleteById(workoutId);
    }

    @Test
    void testCreateWorkout_WithForm() {
        UUID userId = UUID.randomUUID();
        org.delcom.app.dto.WorkoutForm form = new org.delcom.app.dto.WorkoutForm();
        form.setTitle("Form Title");
        form.setDescription("Form Desc");
        form.setDurationMinutes(20);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        Workout workout = new Workout();
        lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

        Workout created = workoutService.createWorkout(form, userId);
        assertNotNull(created);
    }

    @Test
    void testUpdateWorkout_WithForm() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        org.delcom.app.dto.WorkoutForm form = new org.delcom.app.dto.WorkoutForm();
        form.setId(workoutId);
        form.setTitle("Form Title");
        form.setDescription("Form Desc");
        form.setDurationMinutes(20);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        Workout existing = new Workout();
        existing.setId(workoutId);

        lenient().when(workoutRepository.findByUserIdAndId(userId, workoutId))
                .thenReturn(java.util.Optional.of(existing));
        lenient().when(workoutRepository.save(any(Workout.class))).thenReturn(existing);

        Workout updated = workoutService.updateWorkout(form, userId);
        assertNotNull(updated);
    }

    @Test
    void testGetDashboardStats_WithData() {
        UUID userId = UUID.randomUUID();

        // Mock repository to return values
        when(workoutRepository.sumDurationByUserId(userId)).thenReturn(120);
        when(workoutRepository.sumCaloriesByUserId(userId)).thenReturn(500.0);
        when(workoutRepository.countByUserId(userId)).thenReturn(5);

        var stats = workoutService.getDashboardStats(userId);

        assertNotNull(stats);
        assertEquals(120, stats.get("totalDuration"));
        assertEquals(500.0, stats.get("totalCalories"));
        assertEquals(5, stats.get("totalWorkouts"));
    }

    @Test
    void testDeleteWorkout_NotFound() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();

        when(workoutRepository.findByUserIdAndId(userId, workoutId)).thenReturn(java.util.Optional.empty());

        boolean result = workoutService.deleteWorkout(userId, workoutId);

        assertFalse(result);
        verify(workoutRepository, Mockito.never()).deleteById(any(UUID.class));
    }
}
