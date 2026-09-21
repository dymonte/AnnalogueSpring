package com.annalogue.m2.services;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldLoadUserByEmail() {

        // Given
        UserEntity userEntity = new UserEntity(
                "test@test.com",
                "fake-hash",
                "test",
                Role.ETUDIANT,
                2026,
                "G1");

        when(userService.findByEmail("test@test.com"))
                .thenReturn(Optional.of(userEntity));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername("test@test.com");

        // Then
        assertNotNull(result);

        assertEquals("test@test.com", result.getUsername());
        assertEquals("fake-hash", result.getPassword());

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ETUDIANT")));

        verify(userService).findByEmail("test@test.com");
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        // Given
        when(userService.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService
                        .loadUserByUsername("unknown@test.com"));

        verify(userService).findByEmail("unknown@test.com");
    }
}