package org.delcom.app.views;

import org.delcom.app.dto.WorkoutForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.WorkoutService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeView {

    private final WorkoutService workoutService;

    public HomeView(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public String home(Model model,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String filterType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // Dashboard Stats
        var stats = workoutService.getDashboardStats(authUser.getId());
        model.addAttribute("stats", stats);

        // Workouts
        var workouts = workoutService.getAllWorkouts(authUser.getId(), "", filterType);
        model.addAttribute("workouts", workouts);

        // Workout Form
        model.addAttribute("workoutForm", new WorkoutForm());
        model.addAttribute("types", org.delcom.app.enums.WorkoutType.values());

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}
