package com.annalogue.m2;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                        "Groupe 1")
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
                        "Groupe 1")
        );
    }
}