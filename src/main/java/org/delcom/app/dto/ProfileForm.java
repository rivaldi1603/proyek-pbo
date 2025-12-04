package org.delcom.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ProfileForm {

    @NotBlank(message = "Nama harus diisi")
    private String name;

    @NotBlank(message = "Email harus diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    private String bio;
    private String preferences;
    private String favoriteWorkoutType;
    private Integer weeklyDurationGoal;
    private Integer dailyCalorieGoal;

    public ProfileForm() {
    }

    public ProfileForm(String name, String email, String bio, String preferences, String favoriteWorkoutType,
            Integer weeklyDurationGoal, Integer dailyCalorieGoal) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.preferences = preferences;
        this.favoriteWorkoutType = favoriteWorkoutType;
        this.weeklyDurationGoal = weeklyDurationGoal;
        this.dailyCalorieGoal = dailyCalorieGoal;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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
}
