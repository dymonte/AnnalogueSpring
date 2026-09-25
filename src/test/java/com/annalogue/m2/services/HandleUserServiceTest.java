package com.annalogue.m2;

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
                () -> handleUserService.addUser(
                        "test@example.com",
                        Role.ETUDIANT,
                        2026,
                        "A")
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
                "jean.dupont@univ.fr",
                Role.ETUDIANT,
                2026,
                "G1");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertEquals("hashed-password", saved.getHashMdp());
        assertNotEquals(temporaryPassword, saved.getHashMdp());

        verify(passwordEncoder).encode(temporaryPassword);
    }

    @Test
    void shouldSaveRoleYearAndGroup() {
        when(userService.existsByEmail("prof@univ.fr"))
                .thenReturn(false);

        handleUserService.addUser(
                "prof@univ.fr",
                Role.PROF,
                2026,
                "G2");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertEquals("prof@univ.fr", saved.getEmail());
        assertEquals(Role.PROF, saved.getRole());
        assertEquals(2026, saved.getAnnee());
        assertEquals("G2", saved.getGroupe());
    }

    @Test
    void shouldGenerateIdentifiantFromEmailWithDot() {
        when(userService.existsByEmail("jean.dupont@univ.fr"))
                .thenReturn(false);

        handleUserService.addUser(
                "jean.dupont@univ.fr",
                Role.ETUDIANT,
                2026,
                "G1");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());

        assertEquals("jdupont", captor.getValue().getIdentifiant());
    }

    @Test
    void shouldGenerateIdentifiantFromEmailWithoutDot() {
        when(userService.existsByEmail("jdupont@univ.fr"))
                .thenReturn(false);

        handleUserService.addUser(
                "jdupont@univ.fr",
                Role.ETUDIANT,
                2026,
                "G1");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService).save(captor.capture());

        assertEquals("jdupont", captor.getValue().getIdentifiant());
    }

    @Test
    void shouldReturnTemporaryPassword() {
        when(userService.existsByEmail("new@example.com"))
                .thenReturn(false);

        String temporaryPassword = handleUserService.addUser(
                "new@example.com",
                Role.ETUDIANT,
                2026,
                "A");

        assertNotNull(temporaryPassword);
        assertFalse(temporaryPassword.isBlank());
    }
}