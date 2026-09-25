package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.EmailAlreadyExistsException;
import com.annalogue.m2.exceptions.InvalidUserDataException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HandleUserService {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String GROUP_PATTERN = "^[\\p{L}0-9 _-]{1,100}$";
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public HandleUserService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public String addUser(String email, Role role, String username, Integer year, String group) {
        validate(email, role, username, year, group);

        String normalizedEmail = email.trim().toLowerCase();

        if (userService.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Account with this email already exists");
        }

        String temporaryPassword = generateTemporaryPassword();
        String passwordHash = passwordEncoder.encode(temporaryPassword);

        UserEntity user = new UserEntity(
                normalizedEmail,
                passwordHash,
                generateUsername(normalizedEmail),
                role,
                year,
                group.trim());
        userService.save(user);

        return temporaryPassword;
    }

    // abdelmadjid.kahoul@univ.fr -> akahoul (same rule as handle-user.js) its for the back side to check so !!

    public String generateUsername(String email) {
        String name = email.trim().toLowerCase();
        name = name.substring(0, name.indexOf("@"));

        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            return parts[0].charAt(0) + parts[1];
        }

        return name;
    }

    private void validate(String email, Role role, String username, Integer year, String group) {
        if (email == null) {
            throw new InvalidUserDataException("email", "error.user.email.null");
        }
        if (email.isBlank()) {
            throw new InvalidUserDataException("email", "error.user.email.empty");
        }
        if (!email.trim().matches(EMAIL_PATTERN)) {
            throw new InvalidUserDataException("email", "error.user.email.invalid");
        }
        if (role == null) {
            throw new InvalidUserDataException("role", "error.user.role.empty");
        }
        if (username == null || username.isBlank()) {
            throw new InvalidUserDataException("username", "error.user.username.empty");
        }
        // the username sent by the front must be the one the backend generates
        if (!username.trim().equals(generateUsername(email))) {
            throw new InvalidUserDataException("username", "error.user.username.mismatch");
        }
        if (year == null) {
            throw new InvalidUserDataException("year", "error.user.year.empty");
        }
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new InvalidUserDataException("year", "error.user.year.invalid");
        }
        if (group == null || group.isBlank()) {
            throw new InvalidUserDataException("group", "error.user.group.empty");
        }
        if (!group.trim().matches(GROUP_PATTERN)) {
            throw new InvalidUserDataException("group", "error.user.group.invalid");
        }
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}