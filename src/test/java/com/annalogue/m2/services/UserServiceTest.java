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

                // Given
                UserEntity user = mock(UserEntity.class);

                when(userRepository.findByEmail("test@test.com"))
                                .thenReturn(Optional.of(user));

                // When
                Optional<UserEntity> result = userService.findByEmail("test@test.com");

                // Then
                assertTrue(result.isPresent());
                assertEquals(user, result.get());

                verify(userRepository)
                                .findByEmail("test@test.com");
        }
}