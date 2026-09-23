package com.annalogue.m2.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Principal;

@Controller
public class HomeController {

    String username = "";

    @GetMapping("/home")
    public String home(Principal principal, Model model) {

        if (principal != null) {
            username = principal.getName();
        }
        model.addAttribute("username", username);
        return "home";
    }
}
