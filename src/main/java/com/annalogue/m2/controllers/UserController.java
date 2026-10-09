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

import com.annalogue.m2.dto.UsernameCheckResponse;
import com.annalogue.m2.models.StudyLevel;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

import com.annalogue.m2.exceptions.UserDeletionException;

import com.annalogue.m2.services.NotifyService;

import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserController {

    private final RoleService roleService;
    private final HandleUserService handleUserService;
    private final HandPasswordService handPasswordService;
    private final NotifyService notifyService;


    public UserController(
            RoleService roleService,
            HandleUserService handleUserService,
            HandPasswordService handPasswordService,
            NotifyService notifyService) {
        this.roleService = roleService;
        this.handleUserService = handleUserService;
        this.handPasswordService = handPasswordService;
        this.notifyService = notifyService;
    }

    @GetMapping("/members/add")
    public String showForm(Model model) {
        addFormLists(model);
        return "admin/add_member";
    }

    @PostMapping("/members/add")
    public String addMember(@RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String group,
            Model model,
            RedirectAttributes redirectAttributes) {

        // role: required and must exist (RoleService, TFAN26-11)
        if (role == null || role.isBlank()) {
            model.addAttribute("roleErrorKey", "error.user.role.empty");
            return showFormAgain(model, email, role, username, year, level, group);
        }
        if (!roleService.exists(role)) {
            model.addAttribute("roleErrorKey", "error.user.role.invalid");
            return showFormAgain(model, email, role, username, year, level, group);
        }

        try {
            handleUserService.addUser(email, roleService.fromString(role), username, year, level, group);

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

        return showFormAgain(model, email, role, username, year, level, group);
    }

    // shows the form again with what the admin typed
    private String showFormAgain(Model model, String email, String role,
            String username, Integer year, String level, String group) {
        addFormLists(model);
        model.addAttribute("email", email);
        model.addAttribute("selectedRole", role);
        model.addAttribute("username", username);
        model.addAttribute("year", year);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("group", group);
        return "admin/add_member";
    }
        // members list with a delete button (admin only, see SecurityConfig: /members/**)
    @GetMapping("/members")
    public String listMembers(Model model) {
        model.addAttribute("users", handleUserService.findAllUsers());
        return "admin/members";
    }

    // the user is identified by his id (the username is not unique)
    @PostMapping("/members/{id}/delete")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String deletedUsername = handleUserService.deleteUser(id);

            redirectAttributes.addFlashAttribute("successKey", "success.member.deleted");
            redirectAttributes.addFlashAttribute("deletedUsername", deletedUsername);

        } catch (UserDeletionException e) {
            // unknown user, admin, or user still linked to something
            redirectAttributes.addFlashAttribute("errorKey", e.getMessageKey());

        } catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute("errorKey", "error.user.delete.database");
        }

        // back to the list in both cases, with the message
        return "redirect:/members";
    }
    // lists shown in the form's dropdowns
    private void addFormLists(Model model) {
        model.addAttribute("roles", roleService.getAssignableRoles());
        model.addAttribute("academicYears", academicYears());
        model.addAttribute("levels", StudyLevel.values());
    }

    // start years offered: 5 years back to 5 years ahead
    // (2026 is displayed as "2026-2027" in the form)
    private List<Integer> academicYears() {
        int currentYear = Year.now().getValue();
        return IntStream.rangeClosed(currentYear - 5, currentYear + 5)
                .boxed()
                .toList();
    }

    // instant check while the admin types the username (called by handle-user.js)
    @GetMapping("/members/username-check")
    @ResponseBody
    public UsernameCheckResponse checkUsername(@RequestParam String username) {
        boolean available = handleUserService.isUsernameAvailable(username);
        String suggestion = available ? username.trim() : handleUserService.suggestUsername(username);
        return new UsernameCheckResponse(available, suggestion);
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
                    notifyService.getMessage("success.password.changed"));

            return "redirect:/home";

        } catch (UsernameNotFoundException e) {
            // the exception messages are keys of messages.properties
            model.addAttribute("error", notifyService.getMessage(e.getMessage()));

        } catch (InvalidPasswordException e) {
            model.addAttribute("currentPasswordError", notifyService.getMessage(e.getMessage()));

        } catch (PasswordMismatchException e) {
            model.addAttribute("confirmPasswordError", notifyService.getMessage(e.getMessage()));

        } catch (WeakPasswordException e) {
            model.addAttribute("newPasswordError", notifyService.getMessage(e.getMessage()));
        }

        return "change_password";
    }

}