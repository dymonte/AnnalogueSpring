package com.annalogue.m2.controllers;

import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.models.NotificationType;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    private final INotificationService notificationService = mock(INotificationService.class);
    private final IUserService userService = mock(IUserService.class);

    private final UserEntity student =
            new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, 2026, "A");

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        student.setId(5L);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificationController(notificationService, userService))
                .build();
    }

    private UsernamePasswordAuthenticationToken auth(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email, "pw", List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void shouldShowTheNotificationsOfTheUser() throws Exception {
        List<NotificationEntity> notifications =
                List.of(new NotificationEntity(student, NotificationType.MODULE_ENROLLMENT, "MOIA"));
        when(userService.findByEmail("memb@example.com")).thenReturn(Optional.of(student));
        when(notificationService.findForUser(5L)).thenReturn(notifications);

        mockMvc.perform(get("/notifications").principal(auth("memb@example.com", "ETUDIANT")))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications/list"))
                .andExpect(model().attribute("notifications", notifications))
                .andExpect(model().attribute("unseenNotifications", 0L))
                .andExpect(model().attribute("isAdmin", false));
    }

    @Test
    void shouldMarkThemAsSeenAfterReadingThem() throws Exception {
        when(userService.findByEmail("memb@example.com")).thenReturn(Optional.of(student));
        when(notificationService.findForUser(5L)).thenReturn(List.of());

        mockMvc.perform(get("/notifications").principal(auth("memb@example.com", "ETUDIANT")));

        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).findForUser(5L);
        inOrder.verify(notificationService).markAllAsSeen(5L);
    }

    @Test
    void shouldShowAnEmptyListWhenTheUserIsNotFound() throws Exception {
        when(userService.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/notifications").principal(auth("ghost@example.com", "ETUDIANT")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("notifications", List.of()));

        verify(notificationService, never()).findForUser(anyLong());
        verify(notificationService, never()).markAllAsSeen(anyLong());
    }

    @Test
    void shouldTellThePageThatTheUserIsAdmin() throws Exception {
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/notifications").principal(auth("admin@example.com", "ADMIN")))
                .andExpect(model().attribute("isAdmin", true));
    }
}