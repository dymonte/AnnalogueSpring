package com.annalogue.m2.controllers;

import com.annalogue.m2.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private IUserService userService;

    @ModelAttribute
    public void addGlobalAttributes(Authentication authentication, Model model) {
        if (authentication != null) {
            userService.findByEmail(authentication.getName()).ifPresent(
                    userEntity -> model.addAttribute("username", userEntity.getUsername()));
        }
    }
}
