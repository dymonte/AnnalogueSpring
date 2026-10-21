package com.annalogue.m2.services;

import com.annalogue.m2.models.*;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.NotificationRepository;
import com.annalogue.m2.exceptions.NotificationStateException;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<NotificationEntity> notificationCaptor;

    @Captor
    private ArgumentCaptor<List<NotificationEntity>> listCaptor;

    private UserEntity responsible;
    private UserEntity student;
    private UserEntity lecturer;
    private ModuleEntity tf;

    @BeforeEach
    void setUp() {
        responsible = new UserEntity("resp@example.com", "hash", "resp", Role.RESPONSABLE, null, null);
        responsible.setId(1L);
        student = new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, 2026, "A");
        student.setId(2L);
        lecturer = new UserEntity("prof@example.com", "hash", "prof", Role.PROF, null, null);
        lecturer.setId(3L);

        tf = new ModuleEntity("TF", responsible);
        tf.setId(10L);
    }

    private PublicationEntity subjectBy(UserEntity author) {
        return new PublicationEntity("Exam 2025", "", FileType.EXAM, tf, author, "/tmp/exam.pdf");
    }

    private List<NotificationEntity> savedList() {
        verify(notificationRepository).saveAll(listCaptor.capture());
        return listCaptor.getValue();
    }

    private List<UserEntity> savedRecipients() {
        return savedList().stream().map(NotificationEntity::getRecipient).toList();
    }

    // ===== reading (TFAN26-111) =====

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

    // ===== creation for the actions (TFAN26-115) =====

    @Test
    void shouldNotifyTheNewUser() {
        notificationService.notifyAccountCreated(student);

        verify(notificationRepository).save(notificationCaptor.capture());
        NotificationEntity notification = notificationCaptor.getValue();
        assertEquals(student, notification.getRecipient());
        assertEquals(NotificationType.ACCOUNT_CREATED, notification.getType());
        assertNull(notification.getContent());
        assertFalse(notification.isSeen());
    }

    @Test
    void shouldNotifyTheEnrolledUserWithTheModuleName() {
        notificationService.notifyEnrollment(student, tf);

        verify(notificationRepository).save(notificationCaptor.capture());
        assertEquals(student, notificationCaptor.getValue().getRecipient());
        assertEquals(NotificationType.MODULE_ENROLLMENT, notificationCaptor.getValue().getType());
        assertEquals("TF", notificationCaptor.getValue().getContent());
    }

    @Test
    void shouldNotifyEveryoneInTheModuleExceptTheAuthor() {
        when(inscriptionRepository.findByModuleId(10L)).thenReturn(List.of(
                new InscriptionEntity(InscriptionType.ETUDIANT, student, tf),
                new InscriptionEntity(InscriptionType.INTERVENANT, lecturer, tf)));

        notificationService.notifyPublicationAdded(subjectBy(lecturer));

        List<NotificationEntity> notifications = savedList();
        assertEquals(List.of(responsible, student),
                notifications.stream().map(NotificationEntity::getRecipient).toList());
        assertTrue(notifications.stream()
                .allMatch(n -> n.getType() == NotificationType.PUBLICATION_ADDED && "TF".equals(n.getContent())));
    }

    @Test
    void shouldNotNotifyTheResponsableWhoAddedTheSubject() {
        when(inscriptionRepository.findByModuleId(10L)).thenReturn(List.of(
                new InscriptionEntity(InscriptionType.ETUDIANT, student, tf)));

        notificationService.notifyPublicationAdded(subjectBy(responsible));

        assertEquals(List.of(student), savedRecipients());
    }

    @Test
    void shouldNotifyEachUserOnlyOnce() {
        // the responsable is also enrolled in his module (editor)
        when(inscriptionRepository.findByModuleId(10L)).thenReturn(List.of(
                new InscriptionEntity(InscriptionType.INTERVENANT, responsible, tf),
                new InscriptionEntity(InscriptionType.ETUDIANT, student, tf)));

        notificationService.notifyPublicationAdded(subjectBy(lecturer));

        assertEquals(List.of(responsible, student), savedRecipients());
    }

    @Test
    void shouldNotifyNobodyWhenTheAuthorIsAlone() {
        when(inscriptionRepository.findByModuleId(10L)).thenReturn(List.of());

        notificationService.notifyPublicationAdded(subjectBy(responsible));

        assertTrue(savedRecipients().isEmpty());
    }

    // ===== notifications enable disable =====

    @Test
    void shouldNotNotifyTheEnrollmentWhenNotificationsAreOff() {
        student.setNotificationsEnabled(false);

        notificationService.notifyEnrollment(student, tf);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldNotNotifyTheUsersWhoTurnedOffTheirNotifications() {
        lecturer.setNotificationsEnabled(false);
        when(inscriptionRepository.findByModuleId(10L)).thenReturn(List.of(
                new InscriptionEntity(InscriptionType.ETUDIANT, student, tf),
                new InscriptionEntity(InscriptionType.INTERVENANT, lecturer, tf)));

        notificationService.notifyPublicationAdded(subjectBy(responsible));

        assertEquals(List.of(student), savedRecipients());
    }

    @Test
    void shouldTurnOffTheNotifications() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        notificationService.disableNotifications(2L);

        assertFalse(student.isNotificationsEnabled());
        verify(userRepository).save(student);
    }

    @Test
    void shouldTurnOnTheNotifications() {
        student.setNotificationsEnabled(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        notificationService.enableNotifications(2L);

        assertTrue(student.isNotificationsEnabled());
        verify(userRepository).save(student);
    }

    @Test
    void shouldRefuseToTurnOffWhenAlreadyOff() {
        student.setNotificationsEnabled(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        NotificationStateException exception = assertThrows(NotificationStateException.class,
                () -> notificationService.disableNotifications(2L));

        assertEquals("error.notification.already.disabled", exception.getMessageKey());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRefuseToTurnOnWhenAlreadyOn() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        NotificationStateException exception = assertThrows(NotificationStateException.class,
                () -> notificationService.enableNotifications(2L));

        assertEquals("error.notification.already.enabled", exception.getMessageKey());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRefuseAnUnknownUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotificationStateException exception = assertThrows(NotificationStateException.class,
                () -> notificationService.disableNotifications(99L));

        assertEquals("error.notification.user.notfound", exception.getMessageKey());
    }
}