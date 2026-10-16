package com.annalogue.m2.repositories;

import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.models.NotificationType;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private UserEntity student;
    private UserEntity responsible;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        student = userRepository.save(
                new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, 2026, "A"));
        responsible = userRepository.save(
                new UserEntity("resp@example.com", "hash", "resp", Role.RESPONSABLE, null, null));
    }

    private NotificationEntity save(UserEntity user, NotificationType type, String content, boolean seen) {
        NotificationEntity notification = new NotificationEntity(user, type, content);
        notification.setSeen(seen);
        return notificationRepository.saveAndFlush(notification);
    }

    @Test
    void shouldSaveNotificationWithIdAndDate() {
        NotificationEntity saved = save(student, NotificationType.MODULE_ENROLLMENT, "MOIA", false);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void shouldSaveTypeAndContent() {
        NotificationEntity saved = save(student, NotificationType.PUBLICATION_ADDED, "MOIA", false);
        entityManager.clear();

        NotificationEntity found = notificationRepository.findById(saved.getId()).orElseThrow();

        assertEquals(NotificationType.PUBLICATION_ADDED, found.getType());
        assertEquals("MOIA", found.getContent());
        assertEquals("memb@example.com", found.getRecipient().getEmail());
        assertFalse(found.isSeen());
    }

    @Test
    void shouldSaveAccountNotificationWithoutContent() {
        NotificationEntity saved = save(student, NotificationType.ACCOUNT_CREATED, null, false);
        entityManager.clear();

        assertNull(notificationRepository.findById(saved.getId()).orElseThrow().getContent());
    }

    @Test
    void shouldFindTheNotificationsOfTheUserNewestFirst() {
        NotificationEntity first = save(student, NotificationType.MODULE_ENROLLMENT, "MOIA", false);
        NotificationEntity second = save(student, NotificationType.PUBLICATION_ADDED, "MOIA", false);
        save(responsible, NotificationType.ACCOUNT_CREATED, null, false);

        List<Long> ids = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(student.getId())
                .stream()
                .map(NotificationEntity::getId)
                .toList();

        assertEquals(List.of(second.getId(), first.getId()), ids);
    }

    @Test
    void shouldFindNothingForAUserWithoutNotifications() {
        assertTrue(notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(student.getId()).isEmpty());
    }

    @Test
    void shouldCountOnlyTheUnseenNotifications() {
        save(student, NotificationType.MODULE_ENROLLMENT, "MOIA", false);
        save(student, NotificationType.PUBLICATION_ADDED, "MOIA", false);
        save(student, NotificationType.ACCOUNT_CREATED, null, true);

        assertEquals(2, notificationRepository.countByRecipientIdAndSeenFalse(student.getId()));
    }

    @Test
    void shouldMarkAllTheNotificationsOfTheUserAsSeen() {
        save(student, NotificationType.MODULE_ENROLLMENT, "MOIA", false);
        save(student, NotificationType.PUBLICATION_ADDED, "MOIA", false);
        save(responsible, NotificationType.ACCOUNT_CREATED, null, false);

        assertEquals(2, notificationRepository.markAllAsSeen(student.getId()));

        assertEquals(0, notificationRepository.countByRecipientIdAndSeenFalse(student.getId()));
        assertEquals(1, notificationRepository.countByRecipientIdAndSeenFalse(responsible.getId()));
    }

    @Test
    void shouldMarkNothingWhenAllAreAlreadySeen() {
        save(student, NotificationType.ACCOUNT_CREATED, null, true);

        assertEquals(0, notificationRepository.markAllAsSeen(student.getId()));
    }

    @Test
    void shouldDeleteTheNotificationsWhenTheUserIsDeleted() {
        save(student, NotificationType.ACCOUNT_CREATED, null, false);
        save(responsible, NotificationType.ACCOUNT_CREATED, null, false);
        entityManager.clear();

        userRepository.deleteById(student.getId());
        userRepository.flush();

        assertEquals(1, notificationRepository.count());
        assertEquals(0, notificationRepository.countByRecipientIdAndSeenFalse(student.getId()));
    }
}