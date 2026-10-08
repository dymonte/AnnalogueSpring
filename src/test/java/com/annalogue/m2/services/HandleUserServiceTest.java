package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidUserDataException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.StudyLevel;
import com.annalogue.m2.models.UserEntity;
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

@ExtendWith(MockitoExtension.class)
class HandleUserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

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
}