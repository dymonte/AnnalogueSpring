package com.annalogue.m2.controllers;

import com.annalogue.m2.services.NotifyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final NotifyService notifyService;

    public AuthController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            Model model) {

        if (error != null) {
            // same message for wrong password and unknown email (see NotifyService)
            model.addAttribute("error", notifyService.getMessage(NotifyService.LOGIN_GENERIC_KEY));
        }

        return "login";
    }
}