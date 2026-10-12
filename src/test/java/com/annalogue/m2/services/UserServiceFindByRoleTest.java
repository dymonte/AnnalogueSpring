package com.annalogue.m2.services;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceFindByRoleTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InscriptionService inscriptionService;


    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnOnlyUsersWithGivenRole() {

        UserEntity admin = new UserEntity("admin@test.com", "hash", "admin", Role.ADMIN, null, null);
        UserEntity student1 = new UserEntity("s1@test.com", "hash", "s1", Role.ETUDIANT, 2026, "G1");
        UserEntity student2 = new UserEntity("s2@test.com", "hash", "s2", Role.ETUDIANT, 2026, "G2");

        when(userRepository.findAll())
                .thenReturn(List.of(admin, student1, student2));

        List<UserEntity> result = userService.findByRole(Role.ETUDIANT);

        assertEquals(2, result.size());
        assertTrue(result.contains(student1));
        assertTrue(result.contains(student2));
        assertFalse(result.contains(admin));

        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUserHasRole() {

        UserEntity admin = new UserEntity("admin@test.com", "hash", "admin", Role.ADMIN, null, null);

        when(userRepository.findAll())
                .thenReturn(List.of(admin));

        List<UserEntity> result = userService.findByRole(Role.PROF);

        assertTrue(result.isEmpty());
    }
}