package com.annalogue.m2.controllers;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalModelAttributesTest {

    @Mock
    private IUserService userService;

    @Mock
    private ObjectProvider<INotificationService> notificationServiceProvider;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private GlobalModelAttributes globalModelAttributes;

    private final UserEntity student =
            new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, 2026, "A");

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken("memb@example.com", "pw", List.of());
    }

    @Test
    void shouldAddTheUsernameTheStateAndTheNumberOfUnseenNotifications() {
        student.setId(5L);
        when(userService.findByEmail("memb@example.com")).thenReturn(Optional.of(student));
        when(notificationServiceProvider.getIfAvailable()).thenReturn(notificationService);
        when(notificationService.countUnseen(5L)).thenReturn(3L);
        ExtendedModelMap model = new ExtendedModelMap();

        globalModelAttributes.addGlobalAttributes(auth(), model);

        assertEquals("memb", model.get("username"));
        assertEquals(true, model.get("notificationsEnabled"));
        assertEquals(3L, model.get("unseenNotifications"));
    }

    @Test
    void shouldSendThatTheNotificationsAreOff() {
        student.setNotificationsEnabled(false);
        when(userService.findByEmail("memb@example.com")).thenReturn(Optional.of(student));
        when(notificationServiceProvider.getIfAvailable()).thenReturn(null);
        ExtendedModelMap model = new ExtendedModelMap();

        globalModelAttributes.addGlobalAttributes(auth(), model);

        assertEquals(false, model.get("notificationsEnabled"));
        assertFalse(model.containsAttribute("unseenNotifications"));
    }

    @Test
    void shouldAddNothingWhenNotLoggedIn() {
        ExtendedModelMap model = new ExtendedModelMap();

        globalModelAttributes.addGlobalAttributes(null, model);

        assertTrue(model.isEmpty());
    }

    @Test
    void shouldAddNothingWhenTheUserIsNotFound() {
        when(userService.findByEmail("memb@example.com")).thenReturn(Optional.empty());
        ExtendedModelMap model = new ExtendedModelMap();

        globalModelAttributes.addGlobalAttributes(auth(), model);

        assertTrue(model.isEmpty());
    }
}