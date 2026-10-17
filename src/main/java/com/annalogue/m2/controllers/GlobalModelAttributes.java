package com.annalogue.m2.controllers;

import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    private final IUserService userService;
    private final INotificationService notificationService;

    @Autowired
    public GlobalModelAttributes(IUserService userService, INotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Authentication authentication, Model model) {
        model.addAttribute("notifications", List.of());
        model.addAttribute("unseenNotifications", 0L);

        if (authentication != null) {
            userService.findByEmail(authentication.getName()).ifPresent(userEntity -> {
                model.addAttribute("username", userEntity.getUsername());
                List<NotificationEntity> notifications = notificationService.findForUser(userEntity.getId());
                model.addAttribute("notifications", notifications);
                model.addAttribute("unseenNotifications", notificationService.countUnseen(userEntity.getId()));
            });
        }
    }
}
