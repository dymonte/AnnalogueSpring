package com.annalogue.m2.controllers;

import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.models.NotificationType;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalModelAttributesTest {

    private final IUserService userService = mock(IUserService.class);
    private final INotificationService notificationService = mock(INotificationService.class);
    private final GlobalModelAttributes globalModelAttributes =
            new GlobalModelAttributes(userService, notificationService);

    @Test
    void shouldAddTheAuthenticatedUsersNotificationsAndUnreadCount() {
        UserEntity user = student();
        List<NotificationEntity> notifications =
                List.of(new NotificationEntity(user, NotificationType.MODULE_ENROLLMENT, "MOIA"));
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(notificationService.findForUser(user.getId())).thenReturn(notifications);
        when(notificationService.countUnseen(user.getId())).thenReturn(1L);

        ExtendedModelMap model = new ExtendedModelMap();
        globalModelAttributes.addGlobalAttributes(authentication(user.getEmail()), model);

        assertEquals("student", model.get("username"));
        assertEquals(notifications, model.get("notifications"));
        assertEquals(1L, model.get("unseenNotifications"));
    }

    @Test
    void shouldSendThatTheNotificationsAreOnByDefault() {
        UserEntity user = student();
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ExtendedModelMap model = new ExtendedModelMap();
        globalModelAttributes.addGlobalAttributes(authentication(user.getEmail()), model);

        assertEquals(true, model.get("notificationsEnabled"));
    }

    @Test
    void shouldSendThatTheNotificationsAreOff() {
        UserEntity user = student();
        user.setNotificationsEnabled(false);
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ExtendedModelMap model = new ExtendedModelMap();
        globalModelAttributes.addGlobalAttributes(authentication(user.getEmail()), model);

        assertEquals(false, model.get("notificationsEnabled"));
    }

    @Test
    void shouldProvideEmptyNotificationDefaultsForAnonymousUsers() {
        ExtendedModelMap model = new ExtendedModelMap();

        globalModelAttributes.addGlobalAttributes(null, model);

        assertEquals(List.of(), model.get("notifications"));
        assertEquals(0L, model.get("unseenNotifications"));
        assertFalse(model.containsAttribute("username"));
        assertFalse(model.containsAttribute("notificationsEnabled"));
        verifyNoInteractions(userService, notificationService);
    }

    @Test
    void shouldOnlyKeepTheDefaultsWhenTheUserIsNotFound() {
        when(userService.findByEmail("student@example.com")).thenReturn(Optional.empty());

        ExtendedModelMap model = new ExtendedModelMap();
        globalModelAttributes.addGlobalAttributes(authentication("student@example.com"), model);

        assertEquals(List.of(), model.get("notifications"));
        assertEquals(0L, model.get("unseenNotifications"));
        assertFalse(model.containsAttribute("username"));
        assertFalse(model.containsAttribute("notificationsEnabled"));
        verifyNoInteractions(notificationService);
    }

    private UserEntity student() {
        UserEntity user = new UserEntity("student@example.com", "hash", "student", Role.ETUDIANT, 2026, "A");
        user.setId(7L);
        return user;
    }

    private UsernamePasswordAuthenticationToken authentication(String email) {
        return new UsernamePasswordAuthenticationToken(
                email, "pw", List.of(new SimpleGrantedAuthority("ROLE_ETUDIANT")));
    }
}