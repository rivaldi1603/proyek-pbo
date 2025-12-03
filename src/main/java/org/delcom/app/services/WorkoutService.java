package org.delcom.app.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Workout;
import org.delcom.app.enums.WorkoutType;
import org.delcom.app.repositories.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final FileStorageService fileStorageService;

    public WorkoutService(WorkoutRepository workoutRepository, FileStorageService fileStorageService) {
        this.workoutRepository = workoutRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Workout createWorkout(org.delcom.app.dto.WorkoutForm form, UUID userId) {
        return createWorkout(userId, form.getTitle(), form.getDescription(), form.getDurationMinutes(), form.getType(),
                form.getDate());
    }

    @Transactional
    public Workout createWorkout(UUID userId, String title, String description, Integer durationMinutes,
            String typeStr, LocalDate date) {
        WorkoutType type;
        try {
            type = WorkoutType.valueOf(typeStr);
        } catch (Exception e) {
            e.printStackTrace();
            type = WorkoutType.RUNNING; // Default
        }
        Double caloriesBurned = calculateCalories(type, durationMinutes);

        Workout workout = new Workout(userId, title, description, durationMinutes, caloriesBurned, date, type, null);
        return workoutRepository.save(workout);
    }

    public List<Workout> getAllWorkouts(UUID userId, String search, String typeStr) {
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                WorkoutType type = WorkoutType.valueOf(typeStr);
                return workoutRepository
                        .findByUserIdAndType(userId, type, org.springframework.data.domain.Pageable.unpaged())
                        .getContent();
            } catch (IllegalArgumentException e) {
                // Ignore invalid type and return all
            }
        }
        if (search != null && !search.trim().isEmpty()) {
            return workoutRepository.findByKeyword(userId, search);
        }
        return workoutRepository.findAllByUserId(userId);
    }

    public java.util.Map<String, Object> getDashboardStats(UUID userId) {
        Integer totalDuration = workoutRepository.sumDurationByUserId(userId);
        Double totalCalories = workoutRepository.sumCaloriesByUserId(userId);
        Integer totalWorkouts = workoutRepository.countByUserId(userId);

        if (totalDuration == null)
            totalDuration = 0;
        if (totalCalories == null)
            totalCalories = 0.0;
        if (totalWorkouts == null)
            totalWorkouts = 0;

        return java.util.Map.of(
                "totalDuration", totalDuration,
                "totalCalories", totalCalories,
                "totalWorkouts", totalWorkouts);
    }

    public Workout getWorkoutById(UUID userId, UUID id) {
        return workoutRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    @Transactional
    public Workout updateWorkout(org.delcom.app.dto.WorkoutForm form, UUID userId) {
        return updateWorkout(userId, form.getId(), form.getTitle(), form.getDescription(), form.getDurationMinutes(),
                form.getType(), form.getDate());
    }

    @Transactional
    public Workout updateWorkout(UUID userId, UUID id, String title, String description, Integer durationMinutes,
            String typeStr, LocalDate date) {
        Workout workout = workoutRepository.findByUserIdAndId(userId, id).orElse(null);
        if (workout != null) {
            WorkoutType type;
            try {
                type = WorkoutType.valueOf(typeStr);
            } catch (Exception e) {
                e.printStackTrace();
                type = WorkoutType.RUNNING; // Default
            }
            Double caloriesBurned = calculateCalories(type, durationMinutes);

            workout.setTitle(title);
            workout.setDescription(description);
            workout.setDurationMinutes(durationMinutes);
            workout.setCaloriesBurned(caloriesBurned);
            workout.setType(type);
            workout.setDate(date);
            return workoutRepository.save(workout);
        }
        return null;
    }

    private Double calculateCalories(WorkoutType type, Integer durationMinutes) {
        double multiplier = 0;
        switch (type) {
            case RUNNING:
                multiplier = 10;
                break;
            case CYCLING:
                multiplier = 8;
                break;
            case GYM:
                multiplier = 6;
                break;
            case BODYWEIGHT:
                multiplier = 5;
                break;
            case PLANK:
                multiplier = 4;
                break;
            case STRETCHING:
            default:
                multiplier = 3;
                break;
        }
        return (double) (durationMinutes * multiplier);
    }

    public java.util.Map<String, Object> getChartData(UUID userId) {
        // Daily Duration Stats
        List<Object[]> dailyStats = workoutRepository.findDailyDurationStats(userId);
        List<String> dailyLabels = new java.util.ArrayList<>();
        List<Long> dailyData = new java.util.ArrayList<>();

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM");

        for (Object[] row : dailyStats) {
            LocalDate date = (LocalDate) row[0];
            Long duration = (Long) row[1];
            dailyLabels.add(date.format(formatter));
            dailyData.add(duration);
        }

        // Type Stats
        List<Object[]> typeStats = workoutRepository.findTypeStats(userId);
        List<String> typeLabels = new java.util.ArrayList<>();
        List<Long> typeData = new java.util.ArrayList<>();

        for (Object[] row : typeStats) {
            org.delcom.app.enums.WorkoutType type = (org.delcom.app.enums.WorkoutType) row[0];
            Long count = (Long) row[1];
            typeLabels.add(type.name());
            typeData.add(count);
        }

        return java.util.Map.of(
                "dailyLabels", dailyLabels,
                "dailyData", dailyData,
                "typeLabels", typeLabels,
                "typeData", typeData);
    }

    @Transactional
    public boolean deleteWorkout(UUID userId, UUID id) {
        Workout workout = workoutRepository.findByUserIdAndId(userId, id).orElse(null);
        if (workout == null) {
            return false;
        }

        if (workout.getImagePath() != null) {
            fileStorageService.deleteFile(workout.getImagePath());
        }

        workoutRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Workout updateImage(UUID workoutId, String imageFilename) {
        Optional<Workout> workoutOpt = workoutRepository.findById(workoutId);
        if (workoutOpt.isPresent()) {
            Workout workout = workoutOpt.get();

            // Hapus file image lama jika ada
            if (workout.getImagePath() != null) {
                fileStorageService.deleteFile(workout.getImagePath());
            }

            workout.setImagePath(imageFilename);
            return workoutRepository.save(workout);
        }
        return null;
    }
}
