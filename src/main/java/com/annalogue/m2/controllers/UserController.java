package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.*;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;
import com.annalogue.m2.services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UserController {

    private final RoleService roleService;
    private final HandleUserService handleUserService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public UserController(RoleService roleService, HandleUserService handleUserService, UserService userService, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.handleUserService = handleUserService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/members/add")
    public String showForm(Model model) {
        model.addAttribute("roles", roleService.getRoles());
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
        }

        return showFormAgain(model, email, role, username, year, group);
    }

    // shows the form again with what the admin typed
    private String showFormAgain(Model model, String email, String role,
                                 String username, Integer year, String group) {
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("email", email);
        model.addAttribute("selectedRole", role);
        model.addAttribute("username", username);
        model.addAttribute("year", year);
        model.addAttribute("group", group);
        return "admin/add_member";
    }




    @PostMapping("/change-password")
    public String changePassword(
            Authentication auth,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        String email = auth.getName();

        try {
            UserEntity user = userService.findByEmail(email)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Utilisateur introuvable")
                    );

            if (!passwordEncoder.matches(
                    currentPassword,
                    user.getPasswordHash()
            )) {
                throw new InvalidPasswordException();
            }

            if (!newPassword.equals(confirmPassword)) {
                throw new PasswordMismatchException();
            }

            if (!newPassword.matches(
                    "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
            )) {
                throw new WeakPasswordException();
            }

            user.setPasswordHash(
                    passwordEncoder.encode(newPassword)
            );

            userService.save(user);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Mot de passe modifié"
            );

            return "redirect:/change-password";

        } catch (UsernameNotFoundException e) {

            model.addAttribute("error", e.getMessage());
            return "change_password";

        } catch (InvalidPasswordException e) {

            model.addAttribute(
                    "currentPasswordError",
                    e.getMessage()
            );
            return "change_password";

        } catch (PasswordMismatchException e) {

            model.addAttribute(
                    "confirmPasswordError",
                    e.getMessage()
            );
            return "change_password";

        } catch (WeakPasswordException e) {

            model.addAttribute(
                    "newPasswordError",
                    e.getMessage()
            );
            return "change_password";
        }
    }


}