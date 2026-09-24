package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.EmailAlreadyExistsException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final RoleService roleService;
    private final HandleUserService handleUserService;

    public UserController(RoleService roleService, HandleUserService handleUserService) {
        this.roleService = roleService;
        this.handleUserService = handleUserService;
    }

    @GetMapping("/members")
    public String showForm(Model model) {
        model.addAttribute("roles", roleService.getRoles());
        return "admin/add_member";
    }

    @PostMapping("/members")
    public String addMember(@RequestParam String email,
                            @RequestParam Role role,
                            @RequestParam(required = false) Integer annee,
                            @RequestParam(required = false) String groupe,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            String temporaryPassword = handleUserService.addUser(email, role, annee, groupe);

            // print the password until the email logic  is set 
            System.out.println("Mot de passe temporaire pour " + email + " : " + temporaryPassword);

            redirectAttributes.addFlashAttribute("successKey", "success.member.added");
            redirectAttributes.addFlashAttribute("addedEmail", email);
            return "redirect:/members";

        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("emailErrorKey", "error.member.email.exists");

        } catch (Exception e) {
            model.addAttribute("errorKey", "error.server");
        }

        // error: show the form again with what the admin typed
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("email", email);
        model.addAttribute("selectedRole", role.name());
        model.addAttribute("annee", annee);
        model.addAttribute("groupe", groupe);
        return "admin/add_member";
    }
}