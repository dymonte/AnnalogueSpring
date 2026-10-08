package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidPasswordException;
import com.annalogue.m2.exceptions.PasswordMismatchException;
import com.annalogue.m2.exceptions.WeakPasswordException;
import com.annalogue.m2.models.UserEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class HandPasswordService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public HandPasswordService(
            UserService userService,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public void changePassword(
            String email,
            String currentPassword,
            String newPassword,
            String confirmPassword) {
        UserEntity user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("error.password.user.notfound"));

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        if (newPassword == null || !newPassword.matches(
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
            throw new WeakPasswordException();
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userService.save(user);
    }
}