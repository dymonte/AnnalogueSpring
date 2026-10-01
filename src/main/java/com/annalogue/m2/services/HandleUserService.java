package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.EmailAlreadyExistsException;
import com.annalogue.m2.exceptions.InvalidUserDataException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class HandleUserService {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String GROUP_PATTERN = "^[\\p{L}0-9 _-]{1,100}$";
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final UserService userService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public HandleUserService(
            UserService userService,
            MailService mailService,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public String addUser(
            String email,
            Role role,
            String username,
            Integer year,
            String group) {

        validate(email, role, username, year, group);

        String normalizedEmail = email.trim().toLowerCase();

        if (userService.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(
                    "Account with this email already exists");
        }

        String temporaryPassword = generateTemporaryPassword();

        // the password is hashed here, once (UserService.save() no longer hashes)
        String hashedPassword = passwordEncoder.encode(temporaryPassword);

        String generatedUsername = generateUsername(normalizedEmail);

        // only students belong to a group
        String groupToSave = (role == Role.ETUDIANT) ? group.trim() : null;

        UserEntity user = new UserEntity(
                normalizedEmail,
                hashedPassword,
                generatedUsername,
                role,
                year,
                groupToSave);

        userService.save(user);

        // the original temporary password is sent, not the hashed one
        mailService.sendCredentials(
                normalizedEmail,
                generatedUsername,
                temporaryPassword);

        return temporaryPassword;
    }

    // jean.dupont@univ.fr -> jdupont (same rule as handle-user.js)
    public String generateUsername(String email) {
        String name = email.trim().toLowerCase();
        name = name.substring(0, name.indexOf("@"));

        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            return parts[0].charAt(0) + parts[1];
        }

        return name;
    }

    private void validate(
            String email,
            Role role,
            String username,
            Integer year,
            String group) {

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
        // the group is required only for students (disabled in the form for the other roles)
        if (role == Role.ETUDIANT) {
            if (group == null || group.isBlank()) {
                throw new InvalidUserDataException("group", "error.user.group.empty");
            }
            if (!group.trim().matches(GROUP_PATTERN)) {
                throw new InvalidUserDataException("group", "error.user.group.invalid");
            }
        }
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}