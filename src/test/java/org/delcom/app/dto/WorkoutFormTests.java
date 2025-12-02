package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutFormTests {

    private WorkoutForm workoutForm;

    @BeforeEach
    void setUp() {
        workoutForm = new WorkoutForm();
    }

    @Test
    @DisplayName("Default constructor membuat objek dengan nilai default")
    void defaultConstructor_CreatesObjectWithDefaultValues() {
        assertNull(workoutForm.getId());
        assertNull(workoutForm.getTitle());
        assertNull(workoutForm.getDescription());
        assertNull(workoutForm.getDurationMinutes());
        assertNull(workoutForm.getType());
        assertNull(workoutForm.getDate());
    }

    @Test
    @DisplayName("Setter dan Getter untuk date bekerja dengan benar")
    void setterAndGetter_Date_WorksCorrectly() {
        LocalDate date = LocalDate.now();
        workoutForm.setDate(date);
        assertEquals(date, workoutForm.getDate());
    }

    @Test
    @DisplayName("Semua field dapat diset dan diget dengan nilai berbagai tipe")
    void allFields_CanBeSetAndGet_WithVariousValues() {
        // Arrange
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String title = "My Workout";
        String description = "This is a description";
        Integer duration = 45;
        String type = "CARDIO";
        LocalDate date = LocalDate.of(2023, 10, 27);

        // Act
        workoutForm.setId(id);
        workoutForm.setTitle(title);
        workoutForm.setDescription(description);
        workoutForm.setDurationMinutes(duration);
        workoutForm.setType(type);
        workoutForm.setDate(date);

        // Assert
        assertEquals(id, workoutForm.getId());
        assertEquals(title, workoutForm.getTitle());
        assertEquals(description, workoutForm.getDescription());
        assertEquals(duration, workoutForm.getDurationMinutes());
        assertEquals(type, workoutForm.getType());
        assertEquals(date, workoutForm.getDate());
    }

    @Test
    @DisplayName("Field dapat diset dengan null values")
    void fields_CanBeSet_WithNullValues() {
        // Act
        workoutForm.setId(null);
        workoutForm.setTitle(null);
        workoutForm.setDescription(null);
        workoutForm.setDurationMinutes(null);
        workoutForm.setType(null);
        workoutForm.setDate(null);

        // Assert
        assertNull(workoutForm.getId());
        assertNull(workoutForm.getTitle());
        assertNull(workoutForm.getDescription());
        assertNull(workoutForm.getDurationMinutes());
        assertNull(workoutForm.getType());
        assertNull(workoutForm.getDate());
    }

    @Test
    void testWorkoutForm() {
        WorkoutForm form = new WorkoutForm();
        form.setId(UUID.randomUUID());
        form.setTitle("Run");
        form.setDescription("Morning Run");
        form.setDurationMinutes(30);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        assertNotNull(form.getId());
        assertEquals("Run", form.getTitle());
        assertEquals("Morning Run", form.getDescription());
        assertEquals(30, form.getDurationMinutes());
        assertEquals("RUNNING", form.getType());
        assertNotNull(form.getDate());
    }

    @Test
    void testSetters() {
        WorkoutForm form = new WorkoutForm();
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        form.setId(id);
        form.setTitle("Swim");
        form.setDescription("Pool");
        form.setDurationMinutes(45);
        form.setType("SWIMMING");
        form.setDate(date);

        assertEquals(id, form.getId());
        assertEquals("Swim", form.getTitle());
        assertEquals("Pool", form.getDescription());
        assertEquals(45, form.getDurationMinutes());
        assertEquals("SWIMMING", form.getType());
        assertEquals(date, form.getDate());
    }

    @Test
    @DisplayName("Field dapat diset dengan empty strings")
    void fields_CanBeSet_WithEmptyStrings() {
        // Act
        workoutForm.setTitle("");
        workoutForm.setDescription("");

        // Assert
        assertEquals("", workoutForm.getTitle());
        assertEquals("", workoutForm.getDescription());
    }

    @Test
    @DisplayName("Field dapat diset dengan blank strings")
    void fields_CanBeSet_WithBlankStrings() {
        // Act
        workoutForm.setTitle("   ");
        workoutForm.setDescription("   ");

        // Assert
        assertEquals("   ", workoutForm.getTitle());
        assertEquals("   ", workoutForm.getDescription());
    }

    @Test
    @DisplayName("Multiple operations pada object yang sama")
    void multipleOperations_OnSameObject() {
        // First set of values
        UUID id1 = UUID.randomUUID();
        workoutForm.setId(id1);
        workoutForm.setTitle("First Title");
        workoutForm.setDurationMinutes(30);

        assertEquals(id1, workoutForm.getId());
        assertEquals("First Title", workoutForm.getTitle());
        assertEquals(30, workoutForm.getDurationMinutes());

        // Second set of values
        UUID id2 = UUID.randomUUID();
        workoutForm.setId(id2);
        workoutForm.setTitle("Second Title");
        workoutForm.setDurationMinutes(60);

        assertEquals(id2, workoutForm.getId());
        assertEquals("Second Title", workoutForm.getTitle());
        assertEquals(60, workoutForm.getDurationMinutes());
    }
}