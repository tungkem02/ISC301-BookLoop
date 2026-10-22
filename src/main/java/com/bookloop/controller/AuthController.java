package com.bookloop.controller;

import com.bookloop.dto.RegistrationForm;
import com.bookloop.exception.DuplicateEmailException;
import com.bookloop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles student registration and login page rendering.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- Registration ---

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Check password confirmation match first
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "passwords.mismatch",
                    "Passwords do not match.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "email.duplicate", e.getMessage());
            return "auth/register";
        }
    }

    // --- Login ---

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
}
