package com.annalogue.m2.controllers;

import com.annalogue.m2.services.RoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private final RoleService roleService;

    public UserController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/members")
    public String showForm(Model model) {
        model.addAttribute("roles", roleService.getRoles());
        return "add_member";
    }

}