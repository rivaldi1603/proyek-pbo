package org.delcom.app.controllers;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.ChangePasswordForm;
import org.delcom.app.dto.ProfileForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private org.delcom.app.repositories.WorkoutRepository workoutRepository;

    @Autowired
    private AuthContext authContext;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String getProfile(Model model) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        User user = authContext.getAuthUser();

        // Refresh user data from DB to get latest
        user = userService.getUserById(user.getId());

        Integer totalWorkouts = workoutRepository.countByUserId(user.getId());
        if (totalWorkouts == null)
            totalWorkouts = 0;

        model.addAttribute("user", user);
        model.addAttribute("totalWorkouts", totalWorkouts);
        model.addAttribute("profileForm",
                new ProfileForm(user.getName(), user.getEmail(), user.getBio(), user.getPreferences(),
                        user.getFavoriteWorkoutType(), user.getWeeklyDurationGoal(), user.getDailyCalorieGoal()));
        model.addAttribute("changePasswordForm", new ChangePasswordForm());

        return "pages/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm profileForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        User user = authContext.getAuthUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            return "pages/profile";
        }

        userService.updateUser(user.getId(), profileForm.getName(), profileForm.getEmail(), profileForm.getBio(),
                profileForm.getPreferences(), profileForm.getFavoriteWorkoutType(), profileForm.getWeeklyDurationGoal(),
                profileForm.getDailyCalorieGoal());

        // Update auth context
        user.setName(profileForm.getName());
        user.setEmail(profileForm.getEmail());
        user.setBio(profileForm.getBio());
        user.setPreferences(profileForm.getPreferences());
        user.setFavoriteWorkoutType(profileForm.getFavoriteWorkoutType());
        user.setWeeklyDurationGoal(profileForm.getWeeklyDurationGoal());
        user.setDailyCalorieGoal(profileForm.getDailyCalorieGoal());

        redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui.");
        return "redirect:/profile";
    }

    @PostMapping("/photo")
    public String updatePhoto(@RequestParam("photo") MultipartFile photo,
            RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        User user = authContext.getAuthUser();

        if (photo.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Silakan pilih foto terlebih dahulu.");
            return "redirect:/profile";
        }

        try {
            String filename = fileStorageService.storeProfilePhoto(photo, user.getId());
            userService.updateProfilePhoto(user.getId(), filename);
            redirectAttributes.addFlashAttribute("success", "Foto profil berhasil diperbarui.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal mengupload foto.");
        }

        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        User user = authContext.getAuthUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profileForm",
                    new ProfileForm(user.getName(), user.getEmail(), user.getBio(), user.getPreferences(),
                            user.getFavoriteWorkoutType(), user.getWeeklyDurationGoal(), user.getDailyCalorieGoal()));
            return "pages/profile";
        }

        if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password lama salah.");
            return "redirect:/profile";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi password tidak cocok.");
            return "redirect:/profile";
        }

        userService.updatePassword(user.getId(), passwordEncoder.encode(form.getNewPassword()));
        redirectAttributes.addFlashAttribute("success", "Password berhasil diubah.");
        return "redirect:/profile";
    }
}
