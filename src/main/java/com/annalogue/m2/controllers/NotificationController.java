package com.annalogue.m2.controllers;

import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class NotificationController {

    private final INotificationService notificationService;
    private final IUserService userService;

    public NotificationController(INotificationService notificationService, IUserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/notifications")
    public String list(Authentication authentication, Model model) {
        String email = authentication != null ? authentication.getName() : "";

        List<NotificationEntity> notifications = userService.findByEmail(email)
                .map(user -> {
                    List<NotificationEntity> found = notificationService.findForUser(user.getId());
                    notificationService.markAllAsSeen(user.getId());
                    found.forEach(notification -> notification.setSeen(true));
                    return found;
                })
                .orElse(List.of());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unseenNotifications", 0L);
        model.addAttribute("isAdmin", isAdmin(authentication));
        return "notifications/list";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}