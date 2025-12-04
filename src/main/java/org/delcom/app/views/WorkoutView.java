package org.delcom.app.views;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.delcom.app.dto.WorkoutImageForm;
import org.delcom.app.dto.WorkoutForm;
import org.delcom.app.entities.Workout;
import org.delcom.app.entities.User;
import org.delcom.app.enums.WorkoutType;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.WorkoutService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/workouts")
public class WorkoutView {

    private final WorkoutService workoutService;
    private final FileStorageService fileStorageService;

    public WorkoutView(WorkoutService workoutService, FileStorageService fileStorageService) {
        this.workoutService = workoutService;
        this.fileStorageService = fileStorageService;
    }

    @ModelAttribute("workoutTypes")
    public WorkoutType[] getWorkoutTypes() {
        return WorkoutType.values();
    }

    @PostMapping("/add")
    public String postAddWorkout(@Valid @ModelAttribute("workoutForm") WorkoutForm workoutForm,
            org.springframework.validation.BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            System.out.println("DEBUG: User not authenticated in postAddWorkout");
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            System.out.println("DEBUG: Principal is not User instance");
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;

        System.out.println("DEBUG: postAddWorkout called by User ID: " + authUser.getId());
        System.out.println("DEBUG: Form Data - Title: " + workoutForm.getTitle() + ", Type: " + workoutForm.getType());

        if (bindingResult.hasErrors()) {
            model.addAttribute("auth", authUser);
            model.addAttribute("types", WorkoutType.values());
            model.addAttribute("workouts", workoutService.getAllWorkouts(authUser.getId(), "", null));
            model.addAttribute("stats", workoutService.getDashboardStats(authUser.getId()));
            model.addAttribute("addWorkoutModalOpen", true);
            return "pages/activities";
        }

        // Simpan workout
        try {
            var entity = workoutService.createWorkout(workoutForm, authUser.getId());

            if (entity == null) {
                redirectAttributes.addFlashAttribute("error", "Gagal menambahkan workout");
                redirectAttributes.addFlashAttribute("addWorkoutModalOpen", true);
                return "redirect:/activities";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem: " + e.getMessage());
            redirectAttributes.addFlashAttribute("addWorkoutModalOpen", true);
            return "redirect:/activities";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Workout berhasil ditambahkan.");
        return "redirect:/activities";
    }

    @PostMapping("/edit")
    public String postEditWorkout(@Valid @ModelAttribute("workoutForm") WorkoutForm workoutForm,
            org.springframework.validation.BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            System.out.println("DEBUG: User not authenticated in postEditWorkout");
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            System.out.println("DEBUG: Principal is not User instance");
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;

        System.out.println("DEBUG: postEditWorkout called by User ID: " + authUser.getId());
        System.out.println("DEBUG: Form Data - ID: " + workoutForm.getId() + ", Title: " + workoutForm.getTitle());

        if (bindingResult.hasErrors()) {
            model.addAttribute("auth", authUser);
            model.addAttribute("types", WorkoutType.values());
            model.addAttribute("workouts", workoutService.getAllWorkouts(authUser.getId(), "", null));
            model.addAttribute("stats", workoutService.getDashboardStats(authUser.getId()));
            model.addAttribute("editWorkoutModalOpen", true);
            model.addAttribute("editWorkoutModalId", workoutForm.getId());
            return "pages/activities";
        }

        // Update workout
        try {
            var updated = workoutService.updateWorkout(workoutForm, authUser.getId());
            if (updated == null) {
                redirectAttributes.addFlashAttribute("error", "Gagal memperbarui workout");
                redirectAttributes.addFlashAttribute("editWorkoutModalOpen", true);
                redirectAttributes.addFlashAttribute("editWorkoutModalId", workoutForm.getId());
                return "redirect:/activities";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem: " + e.getMessage());
            redirectAttributes.addFlashAttribute("editWorkoutModalOpen", true);
            redirectAttributes.addFlashAttribute("editWorkoutModalId", workoutForm.getId());
            return "redirect:/activities";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Workout berhasil diperbarui.");
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String postDeleteWorkout(@ModelAttribute("workoutForm") WorkoutForm workoutForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;

        // Validasi form
        if (workoutForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID workout tidak valid");
            redirectAttributes.addFlashAttribute("deleteWorkoutModalOpen", true);
            return "redirect:/activities";
        }

        // Periksa apakah workout tersedia
        Workout existingWorkout = workoutService.getWorkoutById(authUser.getId(), workoutForm.getId());
        if (existingWorkout == null) {
            redirectAttributes.addFlashAttribute("error", "Workout tidak ditemukan");
            redirectAttributes.addFlashAttribute("deleteWorkoutModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteWorkoutModalId", workoutForm.getId());
            return "redirect:/";
        }

        // Hapus workout
        boolean deleted = workoutService.deleteWorkout(
                authUser.getId(),
                workoutForm.getId());
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus workout");
            redirectAttributes.addFlashAttribute("deleteWorkoutModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteWorkoutModalId", workoutForm.getId());
            return "redirect:/";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Workout berhasil dihapus.");
        return "redirect:/activities";
    }

    @GetMapping("/{workoutId}")
    public String getDetailWorkout(@PathVariable UUID workoutId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // Ambil workout
        Workout workout = workoutService.getWorkoutById(authUser.getId(), workoutId);
        if (workout == null) {
            return "redirect:/";
        }
        model.addAttribute("workout", workout);

        // Workout Image Form
        WorkoutImageForm workoutImageForm = new WorkoutImageForm();
        workoutImageForm.setId(workoutId);
        model.addAttribute("workoutImageForm", workoutImageForm);

        return ConstUtil.TEMPLATE_PAGES_WORKOUTS_DETAIL;
    }

    @PostMapping("/edit-image")
    public String postEditImageWorkout(@Valid @ModelAttribute("workoutImageForm") WorkoutImageForm workoutImageForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;

        if (workoutImageForm.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "File image tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editImageWorkoutModalOpen", true);
            return "redirect:/workouts/" + workoutImageForm.getId();
        }

        // Check if workout exists
        Workout workout = workoutService.getWorkoutById(authUser.getId(), workoutImageForm.getId());
        if (workout == null) {
            redirectAttributes.addFlashAttribute("error", "Workout tidak ditemukan");
            redirectAttributes.addFlashAttribute("editImageWorkoutModalOpen", true);
            return "redirect:/";
        }

        // Validasi manual file type
        if (!workoutImageForm.isValidImage()) {
            redirectAttributes.addFlashAttribute("error", "Format file tidak didukung. Gunakan JPG, PNG, atau GIF");
            redirectAttributes.addFlashAttribute("editImageWorkoutModalOpen", true);
            return "redirect:/workouts/" + workoutImageForm.getId();
        }

        // Validasi file size (max 5MB)
        if (!workoutImageForm.isSizeValid(5 * 1024 * 1024)) {
            redirectAttributes.addFlashAttribute("error", "Ukuran file terlalu besar. Maksimal 5MB");
            redirectAttributes.addFlashAttribute("editImageWorkoutModalOpen", true);
            return "redirect:/workouts/" + workoutImageForm.getId();
        }

        try {
            // Simpan file
            String fileName = fileStorageService.storeFile(workoutImageForm.getImageFile(), workoutImageForm.getId());

            // Update workout dengan nama file image
            workoutService.updateImage(workoutImageForm.getId(), fileName);

            redirectAttributes.addFlashAttribute("success", "Image berhasil diupload");
            return "redirect:/workouts/" + workoutImageForm.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengupload image");
            redirectAttributes.addFlashAttribute("editImageWorkoutModalOpen", true);
            return "redirect:/workouts/" + workoutImageForm.getId();
        }

    }

    @GetMapping("/image/{filename:.+}")
    @ResponseBody
    public Resource getImageByFilename(@PathVariable String filename) {
        try {
            Path file = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
