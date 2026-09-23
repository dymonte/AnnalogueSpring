package com.annalogue.m2.controllers;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        return "add_member";
    }
    @PostMapping("/members")   
    public String addMember(
            @RequestParam String email,   
            @RequestParam Role role) {

        handleUserService.addUser(email, role);  
        return "redirect:/members";
    }
}