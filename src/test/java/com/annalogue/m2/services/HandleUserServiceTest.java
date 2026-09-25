package com.annalogue.m2;

import com.annalogue.m2.exceptions.InvalidUserDataException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private HandleUserService handleUserService;

    @Test
    void shouldRejectExistingEmail() {
        when(userService.existsByEmail("test@example.com"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> handleUserService.addUser(
                        "test@example.com",
                        Role.ETUDIANT,
                        "test",
                        2026,
                        "A")
        );

        assertEquals(
                "Account with this email already exists",
                exception.getMessage()
        );
    }

    @Test
    void shouldAcceptNewEmail() {
        when(userService.existsByEmail("new@example.com"))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                handleUserService.addUser(
                        "new@example.com",
                        Role.ETUDIANT,
                        "new",
                        2026,
                        "A")
        );
    }

    @Test
    void shouldNotSaveUserWhenEmailExists() {
        when(userService.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> handleUserService.addUser("test@example.com", Role.ETUDIANT, "test", 2026, "A")
        );

        verify(userService, never()).save(any());
    }

    @Test
    void shouldSaveUserWithHashedPassword() {
        when(userService.existsByEmail("jean.dupont@univ.fr"))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed-password");

        String temporaryPassword = handleUserService.addUser(
                "jean.dupont@univ.fr", Role.ETUDIANT, "jdupont", 2026, "A");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertEquals("hashed-password", saved.getPasswordHash());
        assertNotEquals(temporaryPassword, saved.getPasswordHash());

        verify(passwordEncoder).encode(temporaryPassword);
    }

    @Test
    void shouldSaveAllFields() {
        when(userService.existsByEmail("jean.dupont@univ.fr"))
                .thenReturn(false);

        handleUserService.addUser("jean.dupont@univ.fr", Role.PROF, "jdupont", 2026, "B");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertEquals("jean.dupont@univ.fr", saved.getEmail());
        assertEquals("jdupont", saved.getUsername());
        assertEquals(Role.PROF, saved.getRole());
        assertEquals(2026, saved.getYear());
        assertEquals("B", saved.getGroupName());
    }

    @Test
    void shouldGenerateUsernameFromEmailWithDot() {
        assertEquals("jdupont", handleUserService.generateUsername("jean.dupont@univ.fr"));
    }

    @Test
    void shouldGenerateUsernameFromEmailWithoutDot() {
        assertEquals("jdupont", handleUserService.generateUsername("jdupont@univ.fr"));
    }

    @Test
    void shouldReturnTemporaryPassword() {
        when(userService.existsByEmail("new@example.com"))
                .thenReturn(false);

        String temporaryPassword = handleUserService.addUser(
                "new@example.com", Role.ETUDIANT, "new", 2026, "A");

        assertNotNull(temporaryPassword);
        assertFalse(temporaryPassword.isBlank());
    }

    // ===== Validation =====

    private void assertInvalid(String field, String key,
                               String email, Role role, String username, Integer year, String group) {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> handleUserService.addUser(email, role, username, year, group));

        assertEquals(field, exception.getField());
        assertEquals(key, exception.getMessageKey());
        verify(userService, never()).save(any());
    }

    @Test
    void shouldRejectNullEmail() {
        assertInvalid("email", "error.user.email.null",
                null, Role.ETUDIANT, "new", 2026, "A");
    }

    @Test
    void shouldRejectEmptyEmail() {
        assertInvalid("email", "error.user.email.empty",
                "  ", Role.ETUDIANT, "new", 2026, "A");
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        assertInvalid("email", "error.user.email.invalid",
                "not-an-email", Role.ETUDIANT, "new", 2026, "A");
    }

    @Test
    void shouldRejectMissingRole() {
        assertInvalid("role", "error.user.role.empty",
                "new@example.com", null, "new", 2026, "A");
    }

    @Test
    void shouldRejectEmptyUsername() {
        assertInvalid("username", "error.user.username.empty",
                "new@example.com", Role.ETUDIANT, "", 2026, "A");
    }

    @Test
    void shouldRejectUsernameThatDoesNotMatchEmail() {
        assertInvalid("username", "error.user.username.mismatch",
                "jean.dupont@univ.fr", Role.ETUDIANT, "hacker", 2026, "A");
    }

    @Test
    void shouldRejectMissingYear() {
        assertInvalid("year", "error.user.year.empty",
                "new@example.com", Role.ETUDIANT, "new", null, "A");
    }

    @Test
    void shouldRejectYearOutOfRange() {
        assertInvalid("year", "error.user.year.invalid",
                "new@example.com", Role.ETUDIANT, "new", 1990, "A");
    }

    @Test
    void shouldRejectEmptyGroup() {
        assertInvalid("group", "error.user.group.empty",
                "new@example.com", Role.ETUDIANT, "new", 2026, " ");
    }
}