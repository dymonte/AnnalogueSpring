package com.annalogue.m2;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import com.annalogue.m2.services.InscriptionService;
import com.annalogue.m2.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordHashingTest {

        @Mock
        UserRepository userRepository;

        @Mock
        InscriptionService inscriptionService;

        @Test
        void passwordHashingTest() {

                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

                UserService userService = new UserService(userRepository, passwordEncoder,inscriptionService);

                UserEntity user = new UserEntity();

                user.setEmail("fadila@ufr");
                user.setPasswordHash(passwordEncoder.encode("123456"));

                userService.save(user);

                assertTrue(
                                user.getPasswordHash().startsWith("$2a$"));

                assertTrue(
                                passwordEncoder.matches(
                                                "123456",
                                                user.getPasswordHash()));

                verify(userRepository).save(user);
        }
}