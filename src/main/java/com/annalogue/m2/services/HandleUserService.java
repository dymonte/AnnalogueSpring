package com.annalogue.m2.services;

import com.annalogue.m2.models.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class HandleUserService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public HandleUserService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public void addUser(String email, Role role) {
        if (userService.existsByEmail(email)) {
            throw new RuntimeException("This email already exists");
        }
    }
}