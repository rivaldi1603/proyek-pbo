package org.delcom.app.controllers;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.ChangePasswordForm;
import org.delcom.app.dto.ProfileForm;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.WorkoutRepository;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private AuthContext authContext;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProfileController profileController;

    @Test
    @DisplayName("getProfile: Redirect to login if not authenticated")
    void getProfile_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);

        String viewName = profileController.getProfile(model);

        assertEquals("redirect:/auth/login", viewName);
    }

    @Test
    @DisplayName("getProfile: Show profile page if authenticated")
    void getProfile_Authenticated() {
        UUID userId = UUID.randomUUID();
        User user = new User("Test User", "test@example.com", "password");
        user.setId(userId);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(userService.getUserById(userId)).thenReturn(user);
        when(workoutRepository.countByUserId(userId)).thenReturn(5);

        String viewName = profileController.getProfile(model);

        assertEquals("pages/profile", viewName);
        verify(model).addAttribute(eq("user"), any(User.class));
        verify(model).addAttribute(eq("totalWorkouts"), eq(5));
        verify(model).addAttribute(eq("profileForm"), any(ProfileForm.class));
        verify(model).addAttribute(eq("changePasswordForm"), any(ChangePasswordForm.class));
    }

    @Test
    @DisplayName("updateProfile: Redirect to login if not authenticated")
    void updateProfile_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        String viewName = profileController.updateProfile(new ProfileForm(), bindingResult, redirectAttributes, model);
        assertEquals("redirect:/auth/login", viewName);
    }

    @Test
    @DisplayName("updateProfile: Return to form if validation errors")
    void updateProfile_ValidationErrors() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = profileController.updateProfile(new ProfileForm(), bindingResult, redirectAttributes, model);

        assertEquals("pages/profile", viewName);
        verify(model).addAttribute(eq("user"), any(User.class));
    }

    @Test
    @DisplayName("updateProfile: Success")
    void updateProfile_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User("Old Name", "old@example.com", "password");
        user.setId(userId);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Fixed constructor: double -> Integer/Integer match
        ProfileForm form = new ProfileForm("New Name", "new@example.com", "Bio", "Prefs", "Running", 120, 500);

        String viewName = profileController.updateProfile(form, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/profile", viewName);
        // Fixed method call: double -> Integer/Integer match
        verify(userService).updateUser(eq(userId), eq("New Name"), eq("new@example.com"), eq("Bio"), eq("Prefs"),
                eq("Running"), eq(120), eq(500));
        verify(redirectAttributes).addFlashAttribute("success", "Profil berhasil diperbarui.");
    }

    @Test
    @DisplayName("updatePhoto: Error if photo empty")
    void updatePhoto_Empty() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(true);

        String viewName = profileController.updatePhoto(photo, redirectAttributes);

        assertEquals("redirect:/profile", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("updatePhoto: Redirect to login if not authenticated")
    void updatePhoto_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        MultipartFile photo = mock(MultipartFile.class);
        String viewName = profileController.updatePhoto(photo, redirectAttributes);
        assertEquals("redirect:/auth/login", viewName);
    }

    @Test
    @DisplayName("updatePhoto: Success")
    void updatePhoto_Success() throws IOException {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);

        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);
        when(fileStorageService.storeProfilePhoto(photo, userId)).thenReturn("new-photo.jpg");

        String viewName = profileController.updatePhoto(photo, redirectAttributes);

        assertEquals("redirect:/profile", viewName);
        verify(userService).updateProfilePhoto(userId, "new-photo.jpg");
        verify(redirectAttributes).addFlashAttribute("success", "Foto profil berhasil diperbarui.");
    }

    @Test
    @DisplayName("deletePhoto: Success")
    void deletePhoto_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);

        String viewName = profileController.deletePhoto(redirectAttributes);

        assertEquals("redirect:/profile", viewName);
        verify(userService).deleteProfilePhoto(userId);
    }

    @Test
    @DisplayName("changePassword: Old password mismatch")
    void changePassword_OldMismatch() {
        User user = new User();
        user.setPassword("encodedOldPassword");

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setOldPassword("wrongOld");

        when(passwordEncoder.matches("wrongOld", "encodedOldPassword")).thenReturn(false);

        String viewName = profileController.changePassword(form, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/profile", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Password lama salah.");
    }

    @Test
    @DisplayName("changePassword: Confirm password mismatch")
    void changePassword_ConfirmMismatch() {
        User user = new User();
        user.setPassword("encodedOld");

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setOldPassword("correctOld");
        form.setNewPassword("newPass");
        form.setConfirmPassword("mismatch");

        when(passwordEncoder.matches("correctOld", "encodedOld")).thenReturn(true);

        String viewName = profileController.changePassword(form, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/profile", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Konfirmasi password tidak cocok.");
    }

    @Test
    @DisplayName("changePassword: Success")
    void changePassword_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOld");

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setOldPassword("correctOld");
        form.setNewPassword("newPass");
        form.setConfirmPassword("newPass");

        when(passwordEncoder.matches("correctOld", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        String viewName = profileController.changePassword(form, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/profile", viewName);
        verify(userService).updatePassword(userId, "encodedNew");
        verify(redirectAttributes).addFlashAttribute("success", "Password berhasil diubah.");
    }

    @Test
    @DisplayName("updatePhoto: Handle IOException")
    void updatePhoto_IOException() throws IOException {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);

        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);
        when(fileStorageService.storeProfilePhoto(photo, userId)).thenThrow(new IOException("Test Error"));

        String viewName = profileController.updatePhoto(photo, redirectAttributes);

        assertEquals("redirect:/profile", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Gagal mengupload foto.");
    }

    @Test
    @DisplayName("deletePhoto: Redirect to login if not authenticated")
    void deletePhoto_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        String viewName = profileController.deletePhoto(redirectAttributes);
        assertEquals("redirect:/auth/login", viewName);
    }

    @Test
    @DisplayName("changePassword: Redirect to login if not authenticated")
    void changePassword_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        String viewName = profileController.changePassword(new ChangePasswordForm(), bindingResult, redirectAttributes,
                model);
        assertEquals("redirect:/auth/login", viewName);
    }

    @Test
    @DisplayName("changePassword: Return to form if validation errors")
    void changePassword_ValidationErrors() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = profileController.changePassword(new ChangePasswordForm(), bindingResult, redirectAttributes,
                model);

        assertEquals("pages/profile", viewName);
        verify(model).addAttribute(eq("user"), any(User.class));
    }

    @Test
    @DisplayName("getProfile: Handle null total workouts")
    void getProfile_NullTotalWorkouts() {
        UUID userId = UUID.randomUUID();
        User user = new User("Test", "test@example.com", "pw");
        user.setId(userId);

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(userService.getUserById(userId)).thenReturn(user);
        when(workoutRepository.countByUserId(userId)).thenReturn(null); // Return null

        String viewName = profileController.getProfile(model);

        assertEquals("pages/profile", viewName);
        verify(model).addAttribute("totalWorkouts", 0); // Verify default 0
    }
}
