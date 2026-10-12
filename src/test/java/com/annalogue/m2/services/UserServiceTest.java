package com.annalogue.m2.services;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private InscriptionService inscriptionService;

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
        void shouldFindAllUsersWithTheSameUsername() {

                UserEntity first = mock(UserEntity.class);
                UserEntity second = mock(UserEntity.class);

                when(userRepository.findByUsername("jdupont"))
                                .thenReturn(List.of(first, second));

                List<UserEntity> result = userService.findByUsername("jdupont");

                assertEquals(2, result.size());
                verify(userRepository).findByUsername("jdupont");
        }

        @Test
        void shouldReturnEmptyListWhenUsernameDoesNotExist() {

                when(userRepository.findByUsername("unknown"))
                                .thenReturn(List.of());

                assertTrue(userService.findByUsername("unknown").isEmpty());
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

        @Test
        void shouldReturnAllUsersSortedByUsername() {

                List<UserEntity> users = List.of(mock(UserEntity.class));

                when(userRepository.findAllByOrderByUsernameAsc())
                                .thenReturn(users);

                assertEquals(users, userService.findAll());
        }

        @Test
        void shouldFindUserById() {

                UserEntity user = mock(UserEntity.class);

                when(userRepository.findById(5L))
                                .thenReturn(Optional.of(user));

                assertEquals(Optional.of(user), userService.findById(5L));
        }

        @Test
        void shouldDeleteUserAndFlush() {

                UserEntity user = mock(UserEntity.class);

                userService.delete(user);

                verify(userRepository).delete(user);
                verify(userRepository).flush();
        }
}