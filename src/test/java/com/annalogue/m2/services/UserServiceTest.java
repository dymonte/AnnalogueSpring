package com.annalogue.m2.services;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private UserService userService;

        @Test
        void shouldFindUserByEmail() {

                UserEntity user = mock(UserEntity.class);

                when(userRepository.findByEmail("test@test.com"))
                                .thenReturn(Optional.of(user));

                Optional<UserEntity> result = userService.findByEmail("test@test.com");

                assertTrue(result.isPresent());
                assertEquals(user, result.get());

                verify(userRepository).findByEmail("test@test.com");
        }

        @Test
        void shouldReturnEmptyWhenEmailDoesNotExist() {

                when(userRepository.findByEmail("unknown@test.com"))
                                .thenReturn(Optional.empty());

                Optional<UserEntity> result = userService.findByEmail("unknown@test.com");

                assertTrue(result.isEmpty());

                verify(userRepository).findByEmail("unknown@test.com");
        }

        @Test
        void shouldFindUserByIdentifiant() {

                UserEntity user = mock(UserEntity.class);

                when(userRepository.findByIdentifiant("student123"))
                                .thenReturn(Optional.of(user));

                Optional<UserEntity> result = userService.findByIdentifiant("student123");

                assertTrue(result.isPresent());
                assertEquals(user, result.get());

                verify(userRepository)
                                .findByIdentifiant("student123");
        }

        @Test
        void shouldReturnEmptyWhenIdentifiantDoesNotExist() {

                when(userRepository.findByIdentifiant("unknown"))
                                .thenReturn(Optional.empty());

                Optional<UserEntity> result = userService.findByIdentifiant("unknown");

                assertTrue(result.isEmpty());

                verify(userRepository)
                                .findByIdentifiant("unknown");
        }

        @Test
        void shouldCheckIfEmailExists() {

                when(userRepository.existsByEmail("test@test.com"))
                                .thenReturn(true);

                boolean result = userService.existsByEmail("test@test.com");

                assertTrue(result);

                verify(userRepository)
                                .existsByEmail("test@test.com");
        }

        @Test
        void shouldReturnFalseWhenEmailDoesNotExist() {

                when(userRepository.existsByEmail("unknown@test.com"))
                                .thenReturn(false);

                boolean result = userService.existsByEmail("unknown@test.com");

                assertFalse(result);

                verify(userRepository)
                                .existsByEmail("unknown@test.com");
        }

        @Test
        void shouldSaveUser() {

                UserEntity user = mock(UserEntity.class);

                when(userRepository.save(user))
                                .thenReturn(user);

                UserEntity result = userService.save(user);

                assertEquals(user, result);

                verify(userRepository).save(user);
        }
}