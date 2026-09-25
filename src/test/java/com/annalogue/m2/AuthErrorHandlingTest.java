package com.annalogue.m2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthErrorHandlingTest {

    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {

        PasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder();

        UserDetailsService userDetailsService =
                username -> {

                    if (!username.equals("test_user@ufc")) {
                        throw new UsernameNotFoundException(
                                "User not found"
                        );
                    }

                    return User
                            .withUsername("test_user@ufc")
                            .password(
                                    passwordEncoder.encode("123456")
                            )
                            .roles("USER")
                            .build();
                };

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        authenticationManager =
                new ProviderManager(provider);
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotExist() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "unknown@gmail.com",
                        "123456"
                );

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationManager.authenticate(authentication)
        );
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsWrong() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test_user@ufc",
                        "wrongPassword"
                );

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationManager.authenticate(authentication)
        );
    }
}
