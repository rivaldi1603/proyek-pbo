package org.delcom.app.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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

        when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

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

        when(workoutRepository.findByUserIdAndId(userId, workoutId)).thenReturn(java.util.Optional.of(existingWorkout));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Workout updated = workoutService.updateWorkout(userId, workoutId, "Lari Sore", "Lari sore hari", 45, "RUNNING",
                date);

        assertNotNull(updated);
        assertEquals("Lari Sore", updated.getTitle());
        assertEquals(360.0, updated.getCaloriesBurned()); // 45 * 8
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
        when(workoutRepository.save(any(Workout.class))).thenReturn(workout);
        when(workoutRepository.findByKeyword(userId, "Lari")).thenReturn(java.util.List.of(workout));
        when(workoutRepository.findAllByUserId(userId)).thenReturn(java.util.List.of(workout));
        when(workoutRepository.findByUserIdAndId(userId, workoutId)).thenReturn(java.util.Optional.of(workout));
        when(workoutRepository.findByUserIdAndId(userId, nonexistentWorkoutId)).thenReturn(java.util.Optional.empty());
        when(workoutRepository.existsById(workoutId)).thenReturn(true);
        when(workoutRepository.existsById(nonexistentWorkoutId)).thenReturn(false);
        doNothing().when(workoutRepository).deleteById(any(UUID.class));

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

            when(workoutRepository.findByUserIdAndId(userId, workoutWithImageId))
                    .thenReturn(java.util.Optional.of(workoutWithImage));
            when(fileStorageService.deleteFile("image.jpg")).thenReturn(true);

            boolean deleted = workoutService.deleteWorkout(userId, workoutWithImageId);
            assert (deleted == true);
            Mockito.verify(fileStorageService).deleteFile("image.jpg");
        }

        // Menguji method updateImage dengan workout kosong
        {
            workoutId = UUID.randomUUID();
            when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.empty());
            Workout updatedWorkout = workoutService.updateImage(workoutId, "image1.png");
            assert (updatedWorkout == null);
        }

        // Menguji method updateImage dengan sebelumnya ada image
        {
            // Data
            String newImageFilename = "image2.png";

            // Mock
            when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.of(workout));
            when(fileStorageService.deleteFile("image1.png")).thenReturn(true);
            when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

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
            when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.of(workout));
            when(fileStorageService.deleteFile("image1.png")).thenReturn(true);
            when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

            workout.setImagePath(null);
            Workout updatedWorkout = workoutService.updateImage(workoutId, newImageFilename);
            assert (updatedWorkout != null);
            assert (updatedWorkout.getImagePath().equals(newImageFilename));
        }
    }
}
