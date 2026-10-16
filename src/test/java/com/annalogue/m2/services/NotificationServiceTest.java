package com.annalogue.m2.services;

import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.models.NotificationType;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private final UserEntity student =
            new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, 2026, "A");

    @Test
    void shouldFindTheNotificationsOfTheUser() {
        List<NotificationEntity> notifications = List.of(
                new NotificationEntity(student, NotificationType.MODULE_ENROLLMENT, "MOIA"),
                new NotificationEntity(student, NotificationType.ACCOUNT_CREATED, null));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(2L)).thenReturn(notifications);

        assertEquals(notifications, notificationService.findForUser(2L));
    }

    @Test
    void shouldFindNothingForAUserWithoutNotifications() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(2L)).thenReturn(List.of());

        assertTrue(notificationService.findForUser(2L).isEmpty());
    }

    @Test
    void shouldCountTheUnseenNotifications() {
        when(notificationRepository.countByRecipientIdAndSeenFalse(2L)).thenReturn(4L);

        assertEquals(4L, notificationService.countUnseen(2L));
    }

    @Test
    void shouldMarkAllAsSeen() {
        when(notificationRepository.markAllAsSeen(2L)).thenReturn(2);

        assertEquals(2, notificationService.markAllAsSeen(2L));
        verify(notificationRepository).markAllAsSeen(2L);
    }
}