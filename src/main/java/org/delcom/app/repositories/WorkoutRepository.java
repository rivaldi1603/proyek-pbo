package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {
        @Query("SELECT w FROM Workout w WHERE (LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND w.userId = :userId ORDER BY w.date DESC, w.createdAt DESC")
        List<Workout> findByKeyword(UUID userId, String keyword);

        @Query("SELECT w FROM Workout w WHERE w.userId = :userId ORDER BY w.date DESC, w.createdAt DESC")
        List<Workout> findAllByUserId(UUID userId);

        @Query("SELECT w FROM Workout w WHERE w.id = :id AND w.userId = :userId")
        Optional<Workout> findByUserIdAndId(UUID userId, UUID id);

        @Query("SELECT SUM(w.durationMinutes) FROM Workout w WHERE w.userId = :userId")
        Integer sumDurationByUserId(UUID userId);

        @Query("SELECT SUM(w.caloriesBurned) FROM Workout w WHERE w.userId = :userId")
        Double sumCaloriesByUserId(UUID userId);

        @Query("SELECT COUNT(w) FROM Workout w WHERE w.userId = :userId")
        Integer countByUserId(UUID userId);

        org.springframework.data.domain.Page<Workout> findByUserIdAndType(UUID userId,
                        org.delcom.app.enums.WorkoutType type, org.springframework.data.domain.Pageable pageable);

        @Query("SELECT w.date, SUM(w.durationMinutes) FROM Workout w WHERE w.userId = :userId GROUP BY w.date ORDER BY w.date ASC")
        List<Object[]> findDailyDurationStats(UUID userId);

        @Query("SELECT w.type, COUNT(w) FROM Workout w WHERE w.userId = :userId GROUP BY w.type")
        List<Object[]> findTypeStats(UUID userId);
}
