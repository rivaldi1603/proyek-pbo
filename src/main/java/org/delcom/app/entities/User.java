package org.delcom.app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@JsonPropertyOrder({ "id", "name", "email", "createdAt", "updatedAt" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    // ======= Attributes =======
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "preferences")
    private String preferences;

    @Column(name = "favorite_workout_type")
    private String favoriteWorkoutType;

    @Column(name = "weekly_duration_goal")
    private Integer weeklyDurationGoal;

    @Column(name = "daily_calorie_goal")
    private Integer dailyCalorieGoal;

    // ======= Constructors =======

    public User() {
    }

    public User(String email, String password) {
        this("", email, password);
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // ======= Getters and Setters =======
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getFavoriteWorkoutType() {
        return favoriteWorkoutType;
    }

    public void setFavoriteWorkoutType(String favoriteWorkoutType) {
        this.favoriteWorkoutType = favoriteWorkoutType;
    }

    public Integer getWeeklyDurationGoal() {
        return weeklyDurationGoal;
    }

    public void setWeeklyDurationGoal(Integer weeklyDurationGoal) {
        this.weeklyDurationGoal = weeklyDurationGoal;
    }

    public Integer getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void setDailyCalorieGoal(Integer dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ======= @PrePersist & @PreUpdate =======
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
