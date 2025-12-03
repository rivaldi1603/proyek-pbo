package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Workout;
import org.delcom.app.enums.WorkoutType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {
        // 1. Core CRUD & Filter
        Page<Workout> findByUserId(UUID userId, Pageable pageable);

        Page<Workout> findByUserIdAndType(UUID userId, WorkoutType type, Pageable pageable);

        // Keep for Search
        @Query("SELECT w FROM Workout w WHERE (LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND w.userId = :userId ORDER BY w.date DESC, w.createdAt DESC")
        List<Workout> findByKeyword(@Param("userId") UUID userId, @Param("keyword") String keyword);

        // Keep for Secure Edit/Delete
        @Query("SELECT w FROM Workout w WHERE w.id = :id AND w.userId = :userId")
        Optional<Workout> findByUserIdAndId(@Param("userId") UUID userId, @Param("id") UUID id);

        // 2. Statistik Dashboard (Total)
        @Query("SELECT SUM(w.durationMinutes) FROM Workout w WHERE w.userId = :userId")
        Integer sumDurationByUserId(@Param("userId") UUID userId);

        @Query("SELECT SUM(w.caloriesBurned) FROM Workout w WHERE w.userId = :userId")
        Double sumCaloriesByUserId(@Param("userId") UUID userId);

        @Query("SELECT COUNT(w) FROM Workout w WHERE w.userId = :userId")
        Integer countByUserId(@Param("userId") UUID userId);

        // 3. Statistik Chart (Group By)
        @Query("SELECT w.date, SUM(w.durationMinutes) FROM Workout w WHERE w.userId = :userId GROUP BY w.date ORDER BY w.date ASC")
        List<Object[]> findDailyDurationStats(@Param("userId") UUID userId);

        @Query("SELECT w.type, COUNT(w) FROM Workout w WHERE w.userId = :userId GROUP BY w.type")
        List<Object[]> findTypeStats(@Param("userId") UUID userId);
}
