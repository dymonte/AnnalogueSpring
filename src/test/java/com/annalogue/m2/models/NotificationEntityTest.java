package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEntityTest {

    private final UserEntity student =
            new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, 2026, "A");
    private final UserEntity responsible =
            new UserEntity("resp@example.com", "hash", "resp", Role.RESPONSABLE, null, null);

    @Test
    void shouldCreateUnseenNotification() {
        NotificationEntity notification =
                new NotificationEntity(student, NotificationType.MODULE_ENROLLMENT, "MOIA");

        assertEquals(student, notification.getRecipient());
        assertEquals(NotificationType.MODULE_ENROLLMENT, notification.getType());
        assertEquals("MOIA", notification.getContent());
        assertFalse(notification.isSeen());
        assertNull(notification.getId());
    }

    @Test
    void shouldCreateAccountNotificationWithoutContent() {
        NotificationEntity notification =
                new NotificationEntity(student, NotificationType.ACCOUNT_CREATED, null);

        assertNull(notification.getContent());
    }

    @Test
    void shouldMarkAsSeen() {
        NotificationEntity notification =
                new NotificationEntity(student, NotificationType.PUBLICATION_ADDED, "MOIA");

        notification.setSeen(true);

        assertTrue(notification.isSeen());
    }

    @Test
    void shouldChangeRecipientTypeAndContent() {
        NotificationEntity notification = new NotificationEntity();

        notification.setRecipient(responsible);
        notification.setType(NotificationType.PUBLICATION_ADDED);
        notification.setContent("TF");

        assertEquals(responsible, notification.getRecipient());
        assertEquals(NotificationType.PUBLICATION_ADDED, notification.getType());
        assertEquals("TF", notification.getContent());
    }

    @Test
    void shouldGiveTheMessageKeyOfEachType() {
        assertEquals("notification.ACCOUNT_CREATED", NotificationType.ACCOUNT_CREATED.getMessageKey());
        assertEquals("notification.MODULE_ENROLLMENT", NotificationType.MODULE_ENROLLMENT.getMessageKey());
        assertEquals("notification.PUBLICATION_ADDED", NotificationType.PUBLICATION_ADDED.getMessageKey());
    }
}