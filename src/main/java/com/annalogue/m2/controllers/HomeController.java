package com.annalogue.m2.controllers;

import com.annalogue.m2.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.security.core.Authentication;

import java.util.Objects;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping(value = { "/home", "/" })
    public String home(Authentication authentication, Model model) {

        model.addAttribute("login", authentication != null ? authentication.getName() : "");
        model.addAttribute("isAdmin",
                authentication != null
                        && authentication.getAuthorities().stream()
                                .anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN")));

        model.addAttribute("canCreateModuleAndPublication",
                authentication != null
                        && authentication.getAuthorities().stream()
                        .anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN") || Objects.equals(role.getAuthority(), "ROLE_PROF")));


        return "home";
    }
}
