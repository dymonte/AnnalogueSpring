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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

// TFAN26-103: members of a given group
@ExtendWith(MockitoExtension.class)
class UserServiceFindByGroupTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnTheMembersOfTheGroup() {
        UserEntity anna = new UserEntity("anna@test.com", "hash", "anna", Role.ETUDIANT, 2026, "A");
        UserEntity zoe = new UserEntity("zoe@test.com", "hash", "zoe", Role.ETUDIANT, 2026, "A");
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("A"))
                .thenReturn(List.of(anna, zoe));

        List<UserEntity> result = userService.findByGroup("A");

        assertEquals(List.of(anna, zoe), result);
    }

    @Test
    void shouldTrimTheGroupName() {
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("A"))
                .thenReturn(List.of());

        userService.findByGroup("  A  ");

        verify(userRepository).findByGroupNameIgnoreCaseOrderByUsernameAsc("A");
    }

    @Test
    void shouldReturnEmptyListWhenGroupIsNull() {
        assertTrue(userService.findByGroup(null).isEmpty());

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldReturnEmptyListWhenGroupIsBlank() {
        assertTrue(userService.findByGroup("   ").isEmpty());

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldReturnEmptyListWhenGroupDoesNotExist() {
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("Z"))
                .thenReturn(List.of());

        assertTrue(userService.findByGroup("Z").isEmpty());
    }

    @Test
    void shouldReturnDistinctGroupNames() {
        when(userRepository.findDistinctGroupNames()).thenReturn(List.of("Master 2 ISL", "Master 2 IA"));

        assertEquals(List.of("Master 2 ISL", "Master 2 IA"), userService.findAllGroupNames());
    }
}