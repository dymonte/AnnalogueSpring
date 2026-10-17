package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidUserDataException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.StudyLevel;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.annalogue.m2.exceptions.UserDeletionException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class HandleUserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private INotificationService notificationService;
    @InjectMocks
    private HandleUserService handleUserService;

    // ===== Email already used =====

    @Test
    void shouldRejectExistingEmail() {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> handleUserService.addUser("test@example.com", Role.ETUDIANT, "test", 2026, "M1", "A"));

        assertEquals("Account with this email already exists", exception.getMessage());
        verify(userService, never()).save(any());
    }

    @Test
    void shouldAcceptNewEmail() {
        assertDoesNotThrow(() ->
                handleUserService.addUser("new@example.com", Role.ETUDIANT, "newuser", 2026, "M1", "A"));
    }

    // ===== Username edited by the admin =====

    @Test
    void shouldSaveUsernameEditedByTheAdmin() {
        handleUserService.addUser("jean.dupont@univ.fr", Role.ETUDIANT, "jdupont1", 2026, "M1", "A");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());

        assertEquals("jdupont1", captor.getValue().getUsername());
    }

    @Test
    void shouldGenerateUsernameFromEmailWithDot() {
        assertEquals("jdupont", handleUserService.generateUsername("jean.dupont@univ.fr"));
    }

    @Test
    void shouldGenerateUsernameFromEmailWithoutDot() {
        assertEquals("jdupont", handleUserService.generateUsername("jdupont@univ.fr"));
    }

    // ===== Instant check and suggestion =====

    @Test
    void shouldSayUsernameIsAvailable() {
        when(userService.existsByUsername("jkamel")).thenReturn(false);

        assertTrue(handleUserService.isUsernameAvailable("jkamel"));
    }

    @Test
    void shouldSayUsernameIsTaken() {
        when(userService.existsByUsername("jkamel")).thenReturn(true);

        assertFalse(handleUserService.isUsernameAvailable("jkamel"));
    }

    @Test
    void shouldSuggestSameUsernameWhenFree() {
        when(userService.existsByUsername("jkamel")).thenReturn(false);

        assertEquals("jkamel", handleUserService.suggestUsername("jkamel"));
    }

    @Test
    void shouldSuggestUsernameWithNumberWhenTaken() {
        when(userService.existsByUsername("jkamel")).thenReturn(true);
        when(userService.existsByUsername("jkamel1")).thenReturn(false);

        assertEquals("jkamel1", handleUserService.suggestUsername("jkamel"));
    }

    @Test
    void shouldSuggestNextFreeNumber() {
        when(userService.existsByUsername("jkamel")).thenReturn(true);
        when(userService.existsByUsername("jkamel1")).thenReturn(true);
        when(userService.existsByUsername("jkamel2")).thenReturn(false);

        assertEquals("jkamel2", handleUserService.suggestUsername("jkamel"));
    }

    // ===== Password =====

    @Test
    void shouldSaveHashedPasswordAndEmailThePlainOne() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");

        String temporaryPassword = handleUserService.addUser(
                "jean.dupont@univ.fr", Role.ETUDIANT, "jdupont", 2026, "M1", "A");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());

        assertEquals("hashed-password", captor.getValue().getPasswordHash());
        verify(passwordEncoder).encode(temporaryPassword);
        verify(mailService).sendCredentials("jean.dupont@univ.fr", "jdupont", temporaryPassword);
    }

    @Test
    void shouldReturnTemporaryPassword() {
        String temporaryPassword = handleUserService.addUser(
                "new@example.com", Role.ETUDIANT, "newuser", 2026, "M1", "A");

        assertNotNull(temporaryPassword);
        assertFalse(temporaryPassword.isBlank());
    }

    // ===== Saved fields =====

    @Test
    void shouldSaveAllFieldsForStudent() {
        handleUserService.addUser("jean.dupont@univ.fr", Role.ETUDIANT, "jdupont", 2026, "m2", "B");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertEquals("jean.dupont@univ.fr", saved.getEmail());
        assertEquals("jdupont", saved.getUsername());
        assertEquals(Role.ETUDIANT, saved.getRole());
        // 2026 = academic year 2026-2027
        assertEquals(2026, saved.getYear());
        // "m2" is accepted and saved as M2
        assertEquals(StudyLevel.M2, saved.getLevel());
        assertEquals("B", saved.getGroupName());
    }

    @Test
    void shouldAcceptProfessorWithoutYearLevelAndGroup() {
        handleUserService.addUser("prof@univ.fr", Role.PROF, "prof", null, null, null);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());

        assertNull(captor.getValue().getYear());
        assertNull(captor.getValue().getLevel());
        assertNull(captor.getValue().getGroupName());
    }

    @Test
    void shouldIgnoreYearLevelAndGroupSentForProfessor() {
        handleUserService.addUser("prof@univ.fr", Role.PROF, "prof", 2026, "M1", "A");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());

        assertNull(captor.getValue().getYear());
        assertNull(captor.getValue().getLevel());
        assertNull(captor.getValue().getGroupName());
    }

    // ===== Validation =====

    private void assertInvalid(String field, String key,
                               String email, Role role, String username,
                               Integer year, String level, String group) {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> handleUserService.addUser(email, role, username, year, level, group));

        assertEquals(field, exception.getField());
        assertEquals(key, exception.getMessageKey());
        verify(userService, never()).save(any());
    }

    @Test
    void shouldRejectNullEmail() {
        assertInvalid("email", "error.user.email.null", null, Role.ETUDIANT, "newuser", 2026, "M1", "A");
    }

    @Test
    void shouldRejectEmptyEmail() {
        assertInvalid("email", "error.user.email.empty", "  ", Role.ETUDIANT, "newuser", 2026, "M1", "A");
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        assertInvalid("email", "error.user.email.invalid", "not-an-email", Role.ETUDIANT, "newuser", 2026, "M1", "A");
    }

    @Test
    void shouldRejectMissingRole() {
        assertInvalid("role", "error.user.role.empty", "new@example.com", null, "newuser", 2026, "M1", "A");
    }

    @Test
    void shouldRejectAdminRole() {
        assertInvalid("role", "error.user.role.admin.forbidden", "new@example.com", Role.ADMIN, "newuser", 2026, "M1", "A");
    }

    @Test
    void shouldRejectEmptyUsername() {
        assertInvalid("username", "error.user.username.empty", "new@example.com", Role.ETUDIANT, " ", 2026, "M1", "A");
    }

    @Test
    void shouldRejectInvalidUsername() {
        assertInvalid("username", "error.user.username.invalid", "new@example.com", Role.ETUDIANT, "a b!", 2026, "M1", "A");
    }

    @Test
    void shouldRejectMissingYearForStudent() {
        assertInvalid("year", "error.user.year.empty", "new@example.com", Role.ETUDIANT, "newuser", null, "M1", "A");
    }

    @Test
    void shouldRejectYearOutOfRange() {
        assertInvalid("year", "error.user.year.invalid", "new@example.com", Role.ETUDIANT, "newuser", 1990, "M1", "A");
    }

    @Test
    void shouldRejectMissingLevelForStudent() {
        assertInvalid("level", "error.user.level.empty", "new@example.com", Role.ETUDIANT, "newuser", 2026, " ", "A");
    }

    @Test
    void shouldRejectUnknownLevel() {
        assertInvalid("level", "error.user.level.invalid", "new@example.com", Role.ETUDIANT, "newuser", 2026, "M3", "A");
    }

    @Test
    void shouldRejectEmptyGroupForStudent() {
        assertInvalid("group", "error.user.group.empty", "new@example.com", Role.ETUDIANT, "newuser", 2026, "M1", " ");
    }

    // ===== Email sending =====

    @Test
    void shouldSendCredentialsAfterSavingUser() {
        String temporaryPassword = handleUserService.addUser(
                "jean.dupont@univ.fr", Role.ETUDIANT, "jdupont", 2026, "M1", "A");

        InOrder order = Mockito.inOrder(userService, mailService);
        order.verify(userService).save(any(UserEntity.class));
        order.verify(mailService).sendCredentials("jean.dupont@univ.fr", "jdupont", temporaryPassword);
    }

    @Test
    void shouldNotSendEmailWhenUserAlreadyExists() {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> handleUserService.addUser("test@example.com", Role.ETUDIANT, "test", 2026, "M1", "A"));

        verify(mailService, never()).sendCredentials(anyString(), anyString(), anyString());
    }

    // ===== delete user test =====
    private UserEntity user(String username, Role role) {
        return new UserEntity(username + "@example.com", "hash", username, role, null, null);
    }

    @Test
    void shouldReturnAllUsers() {
        List<UserEntity> users = List.of(user("memb", Role.ETUDIANT));
        when(userService.findAll()).thenReturn(users);

        assertEquals(users, handleUserService.findAllUsers());
    }

    @Test
    void shouldDeleteStudentLinkedToNothing() {
        UserEntity student = user("memb", Role.ETUDIANT);
        when(userService.findById(5L)).thenReturn(Optional.of(student));

        String deletedUsername = handleUserService.deleteUser(5L);

        assertEquals("memb", deletedUsername);
        verify(userService).delete(student);
    }

    @Test
    void shouldDeleteTeacherLinkedToNothing() {
        UserEntity teacher = user("respmod", Role.PROF);
        when(userService.findById(6L)).thenReturn(Optional.of(teacher));

        handleUserService.deleteUser(6L);

        verify(userService).delete(teacher);
    }

    @Test
    void shouldRefuseToDeleteUnknownUser() {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        UserDeletionException exception = assertThrows(
                UserDeletionException.class,
                () -> handleUserService.deleteUser(99L));

        assertEquals("error.user.delete.notfound", exception.getMessageKey());
        verify(userService, never()).delete(any());
    }

    @Test
    void shouldRefuseToDeleteAdmin() {
        when(userService.findById(1L)).thenReturn(Optional.of(user("admin", Role.ADMIN)));

        UserDeletionException exception = assertThrows(
                UserDeletionException.class,
                () -> handleUserService.deleteUser(1L));

        assertEquals("error.user.delete.admin", exception.getMessageKey());
        verify(userService, never()).delete(any());
    }

    @Test
    void shouldRefuseToDeleteUserStillLinkedToSomething() {
        UserEntity teacher = user("respmod", Role.PROF);
        when(userService.findById(6L)).thenReturn(Optional.of(teacher));
        // the database refuses: the user is still referenced (module, publication...)
        doThrow(new DataIntegrityViolationException("foreign key"))
                .when(userService).delete(teacher);

        UserDeletionException exception = assertThrows(
                UserDeletionException.class,
                () -> handleUserService.deleteUser(6L));

        assertEquals("error.user.delete.linked", exception.getMessageKey());
    }

    // ===== change role (Prof <-> Responsable Module) =====

    private UserEntity userWithId(long id, String username, Role role) {
        UserEntity user = user(username, role);
        user.setId(id);
        return user;
    }

    private void assertRoleChangeRefused(String expectedKey, Long id, Role newRole) {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> handleUserService.changeRole(id, newRole));

        assertEquals("role", exception.getField());
        assertEquals(expectedKey, exception.getMessageKey());
        verify(userService, never()).save(any());
    }

    @Test
    void shouldPromoteProfToResponsable() {
        UserEntity toto = userWithId(7L, "toto", Role.PROF);
        when(userService.findById(7L)).thenReturn(Optional.of(toto));

        String username = handleUserService.changeRole(7L, Role.RESPONSABLE);

        assertEquals("toto", username);
        assertEquals(Role.RESPONSABLE, toto.getRole());
        verify(userService).save(toto);
    }

    @Test
    void shouldDemoteResponsableWhoManagesNoModule() {
        UserEntity toto = userWithId(7L, "toto", Role.RESPONSABLE);
        when(userService.findById(7L)).thenReturn(Optional.of(toto));
        when(moduleRepository.existsByResponsibleId(7L)).thenReturn(false);

        handleUserService.changeRole(7L, Role.PROF);

        assertEquals(Role.PROF, toto.getRole());
        verify(userService).save(toto);
    }

    @Test
    void shouldRefuseToDemoteResponsableWhoStillManagesModules() {
        UserEntity toto = userWithId(7L, "toto", Role.RESPONSABLE);
        when(userService.findById(7L)).thenReturn(Optional.of(toto));
        when(moduleRepository.existsByResponsibleId(7L)).thenReturn(true);

        assertRoleChangeRefused("error.user.role.change.modules", 7L, Role.PROF);
        assertEquals(Role.RESPONSABLE, toto.getRole());
    }

    @Test
    void shouldNotSaveWhenTheRoleIsTheSame() {
        UserEntity toto = userWithId(7L, "toto", Role.RESPONSABLE);
        when(userService.findById(7L)).thenReturn(Optional.of(toto));

        assertEquals("toto", handleUserService.changeRole(7L, Role.RESPONSABLE));
        verify(userService, never()).save(any());
    }

    @Test
    void shouldRefuseToChangeRoleOfUnknownUser() {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        assertRoleChangeRefused("error.user.role.change.notfound", 99L, Role.RESPONSABLE);
    }

    @Test
    void shouldRefuseEmptyNewRole() {
        when(userService.findById(7L)).thenReturn(Optional.of(userWithId(7L, "toto", Role.PROF)));

        assertRoleChangeRefused("error.user.role.empty", 7L, null);
    }

    @Test
    void shouldRefuseToGiveAdminRole() {
        when(userService.findById(7L)).thenReturn(Optional.of(userWithId(7L, "toto", Role.PROF)));

        assertRoleChangeRefused("error.user.role.change.admin", 7L, Role.ADMIN);
    }

    @Test
    void shouldRefuseToChangeAdminRole() {
        when(userService.findById(1L)).thenReturn(Optional.of(userWithId(1L, "admin", Role.ADMIN)));

        assertRoleChangeRefused("error.user.role.change.admin.user", 1L, Role.PROF);
    }

    @Test
    void shouldRefuseToChangeStudentRole() {
        when(userService.findById(2L)).thenReturn(Optional.of(userWithId(2L, "memb", Role.ETUDIANT)));

        assertRoleChangeRefused("error.user.role.change.notallowed", 2L, Role.RESPONSABLE);
    }

    @Test
    void shouldRefuseToTurnTeacherIntoStudent() {
        when(userService.findById(7L)).thenReturn(Optional.of(userWithId(7L, "toto", Role.PROF)));

        assertRoleChangeRefused("error.user.role.change.notallowed", 7L, Role.ETUDIANT);
    }

    @Test
    void shouldReturnUsersOfOneRole() {
        List<UserEntity> teachers = List.of(user("toto", Role.PROF));
        when(userService.findByRole(Role.PROF)).thenReturn(teachers);

        assertEquals(teachers, handleUserService.findUsersByRole(Role.PROF));
    }


        // ===== Notification of the new user (NOTIF-02) =====

    @Test
    void shouldNotifyTheNewUser() {
        handleUserService.addUser("new@example.com", Role.PROF, "newuser", null, null, null);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(notificationService).notifyAccountCreated(captor.capture());
        assertEquals("new@example.com", captor.getValue().getEmail());
    }

    @Test
    void shouldNotNotifyWhenEmailAlreadyExists() {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> handleUserService.addUser("test@example.com", Role.ETUDIANT, "test", 2026, "M1", "A"));

        verify(notificationService, never()).notifyAccountCreated(any());
    }
}