package org.delcom.app.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileFormTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Constructor and Getters")
    void testConstructorAndGetters() {
        ProfileForm form = new ProfileForm(
                "Name", "email@test.com", "Bio", "Prefs", "RUNNING", 120, 2000);

        assertEquals("Name", form.getName());
        assertEquals("email@test.com", form.getEmail());
        assertEquals("Bio", form.getBio());
        assertEquals("Prefs", form.getPreferences());
        assertEquals("RUNNING", form.getFavoriteWorkoutType());
        assertEquals(120, form.getWeeklyDurationGoal());
        assertEquals(2000, form.getDailyCalorieGoal());
    }

    @Test
    @DisplayName("Test Setters")
    void testSetters() {
        ProfileForm form = new ProfileForm();
        form.setName("Name");
        form.setEmail("email@test.com");
        form.setBio("Bio");
        form.setPreferences("Prefs");
        form.setFavoriteWorkoutType("GYM");
        form.setWeeklyDurationGoal(100);
        form.setDailyCalorieGoal(1500);

        assertEquals("Name", form.getName());
        assertEquals("email@test.com", form.getEmail());
        assertEquals("Bio", form.getBio());
        assertEquals("Prefs", form.getPreferences());
        assertEquals("GYM", form.getFavoriteWorkoutType());
        assertEquals(100, form.getWeeklyDurationGoal());
        assertEquals(1500, form.getDailyCalorieGoal());
    }

    @Test
    @DisplayName("Validation: Success")
    void testValidation_Success() {
        ProfileForm form = new ProfileForm();
        form.setName("Name");
        form.setEmail("email@test.com");

        var violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation: Fail required fields")
    void testValidation_Fail() {
        ProfileForm form = new ProfileForm();
        // Name and Email are @NotBlank

        var violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation: Fail invalid email")
    void testValidation_InvalidEmail() {
        ProfileForm form = new ProfileForm();
        form.setName("Name");
        form.setEmail("invalid-email");

        var violations = validator.validate(form);
        assertEquals(1, violations.size());
        assertEquals("Format email tidak valid", violations.iterator().next().getMessage());
    }
}
