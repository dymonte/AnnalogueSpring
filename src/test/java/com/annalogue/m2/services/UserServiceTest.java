package com.annalogue.m2.services;


import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                .thenReturn(user);

        UserEntity result =
                userService.findByEmail("test@test.com");

        assertNotNull(result);
        assertEquals(user, result);

        verify(userRepository)
                .findByEmail("test@test.com");
    }
}
