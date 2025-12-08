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
                return workoutRepository.findByUserIdAndTypeOrderByDateDesc(userId, type);
            } catch (IllegalArgumentException e) {
                // Ignore invalid type and return all
            }
        }
        if (search != null && !search.trim().isEmpty()) {
            return workoutRepository.findByKeyword(userId, search);
        }
        return workoutRepository.findByUserIdOrderByDateDesc(userId);
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

    public java.util.Map<String, Object> getChartData(UUID userId, String range) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        // --- A. Proses Daily Stats (Duration) ---
        List<Object[]> dailyRows;
        if (range != null && !range.isEmpty()) {
            LocalDate startDate = LocalDate.now();
            if ("week".equalsIgnoreCase(range)) {
                startDate = startDate.minusWeeks(1);
            } else if ("month".equalsIgnoreCase(range)) {
                startDate = startDate.minusMonths(1);
            } else if ("3months".equalsIgnoreCase(range)) {
                startDate = startDate.minusMonths(3);
            } else {
                // Default or "all"
                startDate = null;
            }

            if (startDate != null) {
                dailyRows = workoutRepository.findDailyDurationStatsAfterDate(userId, startDate);
            } else {
                dailyRows = workoutRepository.findDailyDurationStats(userId);
            }
        } else {
            dailyRows = workoutRepository.findDailyDurationStats(userId);
        }
        List<String> dailyLabels = new java.util.ArrayList<>();
        List<Integer> dailyData = new java.util.ArrayList<>();

        if (dailyRows != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM");
            for (Object[] row : dailyRows) {
                if (row[0] != null) {
                    // Handle potential different Date types (java.sql.Date vs LocalDate)
                    if (row[0] instanceof java.sql.Date) {
                        dailyLabels.add(((java.sql.Date) row[0]).toLocalDate().format(formatter));
                    } else if (row[0] instanceof LocalDate) {
                        dailyLabels.add(((LocalDate) row[0]).format(formatter));
                    } else {
                        dailyLabels.add(row[0].toString());
                    }
                }
                if (row[1] != null) {
                    dailyData.add(((Number) row[1]).intValue());
                }
            }
        }

        java.util.Map<String, Object> durationChartMap = new java.util.HashMap<>();
        durationChartMap.put("labels", dailyLabels);
        durationChartMap.put("data", dailyData);
        result.put("duration", durationChartMap);

        // --- B. Proses Type Stats (Count) ---
        List<Object[]> typeRows = workoutRepository.findTypeStats(userId);
        List<String> typeLabels = new java.util.ArrayList<>();
        List<Integer> typeData = new java.util.ArrayList<>();

        if (typeRows != null) {
            for (Object[] row : typeRows) {
                if (row[0] != null) {
                    typeLabels.add(row[0].toString());
                }
                if (row[1] != null) {
                    typeData.add(((Number) row[1]).intValue());
                }
            }
        }

        java.util.Map<String, Object> typeChartMap = new java.util.HashMap<>();
        typeChartMap.put("labels", typeLabels);
        typeChartMap.put("data", typeData);
        result.put("type", typeChartMap);

        return result;
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
