package org.delcom.app.controllers;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.WorkoutForm;
import org.delcom.app.entities.User;
import org.delcom.app.entities.Workout;
import org.delcom.app.services.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutControllerTests {

    @Mock
    private WorkoutService workoutService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private WorkoutController workoutController;

    private User authUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        workoutController.authContext = authContext;
        userId = UUID.randomUUID();
        authUser = new User("Test User", "test@example.com");
        authUser.setId(userId);
    }

    @Test
    @DisplayName("createWorkout: Returns 403 if unauthenticated")
    void createWorkout_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Valid Title");
        form.setDescription("Valid Desc");
        form.setDurationMinutes(30);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("fail", response.getBody().getStatus());
    }

    @Test
    @DisplayName("createWorkout: Validation error (Bad Request)")
    void createWorkout_ValidationError() {
        // No auth context set needed as validation happens first usually?
        // Logic shows validation first.
        WorkoutForm form = new WorkoutForm();
        // Title missing
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fail", response.getBody().getStatus());
    }

    @Test
    @DisplayName("createWorkout: Success")
    void createWorkout_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        WorkoutForm form = new WorkoutForm();
        form.setTitle("Run");
        form.setDescription("Run track");
        form.setDurationMinutes(30);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        Workout created = new Workout();
        created.setId(UUID.randomUUID());

        when(workoutService.createWorkout(eq(userId), eq("Run"), eq("Run track"), eq(30), eq("RUNNING"),
                any(LocalDate.class)))
                .thenReturn(created);

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(created.getId(), response.getBody().getData().get("id"));
    }

    @Test
    @DisplayName("getAllWorkouts: Success")
    void getAllWorkouts_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        List<Workout> workouts = List.of(new Workout());
        when(workoutService.getAllWorkouts(userId, null, null)).thenReturn(workouts);

        ResponseEntity<ApiResponse<Map<String, List<Workout>>>> response = workoutController.getAllWorkouts(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(workouts, response.getBody().getData().get("workouts"));
    }

    @Test
    @DisplayName("getWorkoutById: Not Found")
    void getWorkoutById_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        UUID workoutId = UUID.randomUUID();
        when(workoutService.getWorkoutById(userId, workoutId)).thenReturn(null);

        ResponseEntity<ApiResponse<Map<String, Workout>>> response = workoutController.getWorkoutById(workoutId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getWorkoutById: Success")
    void getWorkoutById_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        UUID workoutId = UUID.randomUUID();
        Workout workout = new Workout();
        workout.setId(workoutId);
        when(workoutService.getWorkoutById(userId, workoutId)).thenReturn(workout);

        ResponseEntity<ApiResponse<Map<String, Workout>>> response = workoutController.getWorkoutById(workoutId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(workout, response.getBody().getData().get("workout"));
    }

    @Test
    @DisplayName("updateWorkout: Success")
    void updateWorkout_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        UUID workoutId = UUID.randomUUID();
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Updated");
        form.setDescription("Updated Desc");
        form.setDurationMinutes(60);
        form.setType("GYM");
        form.setDate(LocalDate.now());

        Workout updated = new Workout();
        updated.setId(workoutId);

        when(workoutService.updateWorkout(eq(userId), eq(workoutId), anyString(), anyString(), anyInt(), anyString(),
                any(LocalDate.class)))
                .thenReturn(updated);

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(workoutId, form);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    @DisplayName("deleteWorkout: Success")
    void deleteWorkout_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        UUID workoutId = UUID.randomUUID();
        when(workoutService.deleteWorkout(userId, workoutId)).thenReturn(true);

        ResponseEntity<ApiResponse<String>> response = workoutController.deleteWorkout(workoutId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("getChartData: Success")
    void getChartData_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        Map<String, Object> mockChart = Map.of("data", "someData");
        when(workoutService.getChartData(userId, "week")).thenReturn(mockChart);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = workoutController.getChartData("week");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockChart, response.getBody().getData());
    }

    @Test
    @DisplayName("getAllWorkouts: Unauthenticated")
    void getAllWorkouts_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, List<Workout>>>> response = workoutController.getAllWorkouts(null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("getWorkoutById: Unauthenticated")
    void getWorkoutById_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, Workout>>> response = workoutController
                .getWorkoutById(UUID.randomUUID());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("updateWorkout: Unauthenticated")
    void updateWorkout_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        WorkoutForm form = new WorkoutForm();
        form.setTitle("T");
        form.setDescription("D");
        form.setDurationMinutes(1);
        form.setType("T");
        form.setDate(LocalDate.now());

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails")
    void updateWorkout_ValidationFails() {
        // Missing title
        WorkoutForm form = new WorkoutForm();
        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateWorkout: Not Found")
    void updateWorkout_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);

        WorkoutForm form = new WorkoutForm();
        form.setTitle("T");
        form.setDescription("D");
        form.setDurationMinutes(1);
        form.setType("T");
        form.setDate(LocalDate.now());

        when(workoutService.updateWorkout(any(), any(), anyString(), anyString(), anyInt(), anyString(), any()))
                .thenReturn(null);

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteWorkout: Unauthenticated")
    void deleteWorkout_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<String>> response = workoutController.deleteWorkout(UUID.randomUUID());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteWorkout: Not Found")
    void deleteWorkout_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(authUser);
        when(workoutService.deleteWorkout(any(), any())).thenReturn(false);

        ResponseEntity<ApiResponse<String>> response = workoutController.deleteWorkout(UUID.randomUUID());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getChartData: Unauthenticated")
    void getChartData_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, Object>>> response = workoutController.getChartData("week");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Missing Title")
    void createWorkout_MissingTitle() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle(null);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data title tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Missing Description")
    void createWorkout_MissingDescription() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription(null);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data description tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Missing Duration")
    void createWorkout_MissingDuration() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(null);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data durationMinutes tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Missing Type")
    void createWorkout_MissingType() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(10);
        form.setType(null);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data type tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Missing Date")
    void createWorkout_MissingDate() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(10);
        form.setType("RUNNING");
        form.setDate(null);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data date tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Empty Title")
    void createWorkout_EmptyTitle() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("");
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data title tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Empty Description")
    void createWorkout_EmptyDescription() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("");
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data description tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Empty Type")
    void createWorkout_EmptyType() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(10);
        form.setType("");
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data type tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Empty Title")
    void updateWorkout_EmptyTitle() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("");
        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data title tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Empty Description")
    void updateWorkout_EmptyDescription() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("");
        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data description tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Empty Type")
    void updateWorkout_EmptyType() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(10);
        form.setType("");
        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data type tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createWorkout: Validation Fails - Negative Duration")
    void createWorkout_NegativeDuration() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(-10);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = workoutController.createWorkout(form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data durationMinutes tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Negative Duration")
    void updateWorkout_NegativeDuration() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(-5);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data durationMinutes tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Null Description")
    void updateWorkout_NullDescription() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription(null); // Null
        form.setDurationMinutes(30);
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data description tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Null Duration")
    void updateWorkout_NullDuration() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(null); // Null
        form.setType("RUNNING");
        form.setDate(LocalDate.now());

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data durationMinutes tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Null Type")
    void updateWorkout_NullType() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(30);
        form.setType(null); // Null
        form.setDate(LocalDate.now());

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data type tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateWorkout: Validation Fails - Null Date")
    void updateWorkout_NullDate() {
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Title");
        form.setDescription("Desc");
        form.setDurationMinutes(30);
        form.setType("RUNNING");
        form.setDate(null); // Null

        ResponseEntity<ApiResponse<Workout>> response = workoutController.updateWorkout(UUID.randomUUID(), form);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data date tidak valid", response.getBody().getMessage());
    }
}
