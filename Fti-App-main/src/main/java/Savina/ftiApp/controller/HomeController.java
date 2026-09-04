package Savina.ftiApp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import Savina.ftiApp.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DepartmentRepository departmentRepo;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("departments", departmentRepo.findAll());
        return "auth/register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "auth/reset-password";
    }

    @GetMapping("/verify")
    public String verifyPage(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer userId, Model model) {
        if (userId != null) {
            model.addAttribute("userId", userId);
        }
        return "auth/verify-code";
    }

    @GetMapping("/student")
    public String student() {
        return "student/index";
    }

    @GetMapping("/nota")
    public String nota() {
        return "student/notat";
    }

    @GetMapping("/mungesat")
    public String mungesat() {
        return "student/mungesat";
    }

    @GetMapping("/administrator")
    public String administrator() {
        return "administrator/dashboard_reports";
    }

    @GetMapping("/petagog")
    public String petagog() {
        return "petagog/rregjistri";
    }

    @GetMapping("/rregjistri")
    public String rregjistri() {
        return "petagog/rregjistri";
    }

    @GetMapping("/lektor")
    public String lektori() {
        return "petagog/lektor";
    }

    @GetMapping("/sidebar")
    public String sidebar() {
        return "student/sidebar";
    }

    @GetMapping("/adm")
    public String adm() {
        return "administrator/pet";
    }

    @GetMapping("/std")
    public String std() {
        return "administrator/std";
    }

    @GetMapping("/lende")
    public String lende() {
        return "administrator/lende";
    }

    @GetMapping("/tc")
    public String tc() {
        return "administrator/tc";
    }

    @GetMapping("/dashboard_reports")
    public String dashboard_reports() {
        return "administrator/dashboard_reports";
    }

    @GetMapping("/course_schedule")
    public String course_schedule() {
        return "administrator/course_schedule";
    }
}
