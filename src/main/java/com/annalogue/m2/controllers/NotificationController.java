package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.NotificationStateException;
import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

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
        Optional<UserEntity> user = userService.findByEmail(emailOf(authentication));

        List<NotificationEntity> notifications = user
                .map(found -> {
                    List<NotificationEntity> list = notificationService.findForUser(found.getId());
                    notificationService.markAllAsSeen(found.getId());
                    return list;
                })
                .orElse(List.of());

        model.addAttribute("notifications", notifications);
        model.addAttribute("notificationsEnabled", user.map(UserEntity::isNotificationsEnabled).orElse(true));
        model.addAttribute("unseenNotifications", 0L);
        model.addAttribute("isAdmin", isAdmin(authentication));
        return "notifications/list";
    }

    @PostMapping("/notifications/disable")
    public String disable(Authentication authentication, RedirectAttributes redirectAttributes) {
        return changeState(authentication, false, redirectAttributes);
    }

    @PostMapping("/notifications/enable")
    public String enable(Authentication authentication, RedirectAttributes redirectAttributes) {
        return changeState(authentication, true, redirectAttributes);
    }

    private String changeState(Authentication authentication, boolean enabled,
                               RedirectAttributes redirectAttributes) {
        Optional<UserEntity> user = userService.findByEmail(emailOf(authentication));
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorKey", "error.notification.user.notfound");
            return "redirect:/notifications";
        }

        try {
            if (enabled) {
                notificationService.enableNotifications(user.get().getId());
            } else {
                notificationService.disableNotifications(user.get().getId());
            }
            redirectAttributes.addFlashAttribute("successKey",
                    enabled ? "success.notification.enabled" : "success.notification.disabled");
        } catch (NotificationStateException e) {
            redirectAttributes.addFlashAttribute("errorKey", e.getMessageKey());
        }

        return "redirect:/notifications";
    }

    private String emailOf(Authentication authentication) {
        return authentication != null ? authentication.getName() : "";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}