package org.delcom.app.views;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.UUID;

import org.delcom.app.dto.WorkoutForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.WorkoutService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class WorkoutViewTests {

    @Mock
    private WorkoutService workoutService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpSession session;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WorkoutView workoutView;

    private User authUser;

    @BeforeEach
    void setUp() {
        authUser = new User("Test User", "test@example.com", "password");
        authUser.setId(UUID.randomUUID());

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testPostAddWorkout_ServiceException() {
        // Mock Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(authUser);

        // Mock Form
        WorkoutForm form = new WorkoutForm();
        form.setTitle("Test Workout");
        form.setType("RUNNING");
        form.setDurationMinutes(30);
        form.setDate(LocalDate.now());

        // Mock Service Exception
        doThrow(new RuntimeException("Error DB")).when(workoutService).createWorkout(any(WorkoutForm.class),
                any(UUID.class));

        // Execute
        String viewName = workoutView.postAddWorkout(form, bindingResult, redirectAttributes, session, model);

        // Verify
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Terjadi kesalahan sistem: Error DB");
        verify(redirectAttributes).addFlashAttribute("addWorkoutModalOpen", true);
    }

    @Test
    void testPostEditWorkout_ServiceException() {
        // Mock Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(authUser);

        // Mock Form
        WorkoutForm form = new WorkoutForm();
        form.setId(UUID.randomUUID());
        form.setTitle("Test Workout");
        form.setType("RUNNING");
        form.setDurationMinutes(30);
        form.setDate(LocalDate.now());

        // Mock Service Exception
        doThrow(new RuntimeException("Error DB")).when(workoutService).updateWorkout(any(WorkoutForm.class),
                any(UUID.class));

        // Execute
        String viewName = workoutView.postEditWorkout(form, bindingResult, redirectAttributes, session, model);

        // Verify
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Terjadi kesalahan sistem: Error DB");
        verify(redirectAttributes).addFlashAttribute("editWorkoutModalOpen", true);
    }

    @Test
    void testPostAddWorkout_ValidationError() {
        // Mock Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(authUser);

        // Mock Validation Error
        when(bindingResult.hasErrors()).thenReturn(true);

        // Execute
        String viewName = workoutView.postAddWorkout(new WorkoutForm(), bindingResult, redirectAttributes, session,
                model);

        // Verify
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, viewName);
        verify(model).addAttribute("addWorkoutModalOpen", true);
    }

    @Test
    void testPostEditWorkout_ValidationError() {
        // Mock Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(authUser);

        // Mock Validation Error
        when(bindingResult.hasErrors()).thenReturn(true);

        WorkoutForm form = new WorkoutForm();
        form.setId(UUID.randomUUID());

        // Execute
        String viewName = workoutView.postEditWorkout(form, bindingResult, redirectAttributes, session, model);

        // Verify
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, viewName);
        verify(model).addAttribute("editWorkoutModalOpen", true);
        verify(model).addAttribute("editWorkoutModalId", form.getId());
    }

    @Test
    void testPostAddWorkout_UserNotLoggedIn() {
        // Mock Not Authenticated
        when(securityContext.getAuthentication()).thenReturn(null);

        // Execute
        String viewName = workoutView.postAddWorkout(new WorkoutForm(), bindingResult, redirectAttributes, session,
                model);

        // Verify
        assertEquals("redirect:/auth/logout", viewName);
    }

    @Test
    void testPostEditWorkout_UserNotLoggedIn() {
        // Mock Not Authenticated
        when(securityContext.getAuthentication()).thenReturn(null);

        WorkoutForm form = new WorkoutForm();
        form.setId(UUID.randomUUID());

        // Execute
        String viewName = workoutView.postEditWorkout(form, bindingResult, redirectAttributes, session, model);

        // Verify
        assertEquals("redirect:/auth/logout", viewName);
    }
}
