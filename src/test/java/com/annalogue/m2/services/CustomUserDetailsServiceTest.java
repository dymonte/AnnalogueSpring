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

        UserEntity userEntity = new UserEntity(
                "test@test.com",
                "fake-hash",
                "test123",
                Role.ETUDIANT,
                2026,
                "G1");

        when(userService.findByEmail("test@test.com"))
                .thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService
                .loadUserByUsername("test@test.com");

        assertNotNull(result);

        assertEquals(
                "test@test.com",
                result.getUsername());

        assertEquals(
                "fake-hash",
                result.getPassword());

        assertTrue(
                result.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority()
                                .equals("ROLE_ETUDIANT")));

        verify(userService)
                .findByEmail("test@test.com");
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userService.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService
                        .loadUserByUsername("unknown@test.com"));

        verify(userService)
                .findByEmail("unknown@test.com");
    }

    @Test
    void shouldUseAdminRole() {

        UserEntity user = new UserEntity(
                "admin@test.com",
                "admin-hash",
                "admin123",
                Role.ADMIN,
                null,
                null);

        when(userService.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService
                .loadUserByUsername("admin@test.com");

        assertTrue(
                result.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority()
                                .equals("ROLE_ADMIN")));
    }

    @Test
    void shouldUseProfessorRole() {

        UserEntity user = new UserEntity(
                "prof@test.com",
                "prof-hash",
                "prof123",
                Role.PROF,
                null,
                null);

        when(userService.findByEmail("prof@test.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService
                .loadUserByUsername("prof@test.com");

        assertTrue(
                result.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority()
                                .equals("ROLE_PROF")));
    }
}