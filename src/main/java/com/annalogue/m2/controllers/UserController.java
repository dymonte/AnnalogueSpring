package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.*;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;
import com.annalogue.m2.services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.annalogue.m2.services.HandPasswordService;

import org.springframework.mail.MailException;

import java.util.Optional;

@Controller
public class UserController {

    private final RoleService roleService;
    private final HandleUserService handleUserService;
    private final HandPasswordService handPasswordService;

    public UserController(
            RoleService roleService,
            HandleUserService handleUserService,
            HandPasswordService handPasswordService) {
        this.roleService = roleService;
        this.handleUserService = handleUserService;
        this.handPasswordService = handPasswordService;
    }

    @GetMapping("/members/add")
    public String showForm(Model model) {
        model.addAttribute("roles", roleService.getAssignableRoles());
        return "admin/add_member";
    }

    @PostMapping("/members/add")
    public String addMember(@RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String group,
            Model model,
            RedirectAttributes redirectAttributes) {

        // role: required and must exist (RoleService, TFAN26-11)
        if (role == null || role.isBlank()) {
            model.addAttribute("roleErrorKey", "error.user.role.empty");
            return showFormAgain(model, email, role, username, year, group);
        }
        if (!roleService.exists(role)) {
            model.addAttribute("roleErrorKey", "error.user.role.invalid");
            return showFormAgain(model, email, role, username, year, group);
        }

        try {
            handleUserService.addUser(email, roleService.fromString(role), username, year, group);

            redirectAttributes.addFlashAttribute("successKey", "success.member.added");
            redirectAttributes.addFlashAttribute("addedEmail", email);
            return "redirect:/members/add";

        } catch (InvalidUserDataException e) {
            // "email" -> emailErrorKey, "year" -> yearErrorKey, ...
            model.addAttribute(e.getField() + "ErrorKey", e.getMessageKey());

        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("emailErrorKey", "error.member.email.exists");

        } catch (DataAccessException e) {
            model.addAttribute("errorKey", "error.database");
        } catch (MailException e) {
            model.addAttribute("errorKey", "error.mail.send");
        }

        return showFormAgain(model, email, role, username, year, group);
    }

    // shows the form again with what the admin typed
    private String showFormAgain(Model model, String email, String role,
            String username, Integer year, String group) {
        model.addAttribute("roles", roleService.getAssignableRoles());
        model.addAttribute("email", email);
        model.addAttribute("selectedRole", role);
        model.addAttribute("username", username);
        model.addAttribute("year", year);
        model.addAttribute("group", group);
        return "admin/add_member";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Authentication auth, Model model) {
        model.addAttribute("isAdmin", auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(role -> java.util.Objects.equals(role.getAuthority(), "ROLE_ADMIN")));
        return "change_password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication auth,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            handPasswordService.changePassword(
                    auth.getName(),
                    currentPassword,
                    newPassword,
                    confirmPassword);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Mot de passe modifié");

            return "redirect:/home";

        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", e.getMessage());

        } catch (InvalidPasswordException e) {
            model.addAttribute(
                    "currentPasswordError",
                    e.getMessage());

        } catch (PasswordMismatchException e) {
            model.addAttribute(
                    "confirmPasswordError",
                    e.getMessage());

        } catch (WeakPasswordException e) {
            model.addAttribute(
                    "newPasswordError",
                    e.getMessage());
        }

        return "change_password";
    }

}