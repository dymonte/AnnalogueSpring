package com.annalogue.m2;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import com.annalogue.m2.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PasswordHashingTest {
    @Test
    public void passwordHashingTest(){
        UserRepository userRepository = mock(UserRepository.class);

        PasswordEncoder passwordEncoder =new BCryptPasswordEncoder();

        UserService userService =new UserService(userRepository,passwordEncoder);

        UserEntity user = new UserEntity();
        user.setEmail("fadila@ufr");
        user.setHashMdp("123456");

        when(userRepository.findByEmail("fadila@ufr"))
                .thenReturn(null);

        userService.save(user);
        assertNotEquals("123456", user.getHashMdp());
        assertTrue(passwordEncoder.matches("123456", user.getHashMdp()));
    }
}
