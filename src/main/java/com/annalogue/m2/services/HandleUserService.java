package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.EmailAlreadyExistsException;
import com.annalogue.m2.exceptions.InvalidUserDataException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.StudyLevel;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.annalogue.m2.exceptions.UserDeletionException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import java.util.UUID;

@Service
public class HandleUserService {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    // letters, digits, dot, dash, underscore - 3 to 50 characters
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]{3,50}$";
    private static final String GROUP_PATTERN = "^[\\p{L}0-9 _-]{1,100}$";
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final UserService userService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final ModuleRepository moduleRepository;

    public HandleUserService(
            UserService userService,
            MailService mailService,
            PasswordEncoder passwordEncoder,
            ModuleRepository moduleRepository) {
        this.userService = userService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.moduleRepository = moduleRepository;
    }

    // year = start of the academic year (2026 for 2026-2027), level = L1, L2, L3, M1, M2
    @Transactional
    public String addUser(
            String email,
            Role role,
            String username,
            Integer year,
            String level,
            String group) {

        validate(email, role, username, year, level, group);

        String normalizedEmail = email.trim().toLowerCase();
        // chosen (or edited) by the admin; a name already used is only a warning in the form
        String chosenUsername = username.trim();

        if (userService.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Account with this email already exists");
        }

        String temporaryPassword = generateTemporaryPassword();

        // the password is hashed here, once (UserService.save() does not hash)
        String hashedPassword = passwordEncoder.encode(temporaryPassword);

        // only students have an academic year, a level and a group
        boolean isStudent = role == Role.ETUDIANT;

        UserEntity user = new UserEntity(
                normalizedEmail,
                hashedPassword,
                chosenUsername,
                role,
                isStudent ? year : null,
                isStudent ? group.trim() : null);
        user.setLevel(isStudent ? StudyLevel.fromString(level) : null);

        userService.save(user);

        // the original temporary password is sent, not the hashed one
        mailService.sendCredentials(normalizedEmail, chosenUsername, temporaryPassword);

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

    // instant check from the form
    public boolean isUsernameAvailable(String username) {
        return !userService.existsByUsername(username.trim());
    }

    // jkamel taken -> jkamel1, jkamel1 taken too -> jkamel2, ...
    public String suggestUsername(String username) {
        String base = username.trim();

        if (isUsernameAvailable(base)) {
            return base;
        }

        int number = 1;
        while (!isUsernameAvailable(base + number)) {
            number++;
        }
        return base + number;
    }
    public List<UserEntity> findAllUsers() {
        return userService.findAll();
    }

    // members list filtered by role
    public List<UserEntity> findUsersByRole(Role role) {
        return userService.findByRole(role);
    }

    // deletes a user linked to nothing; returns his username for the success message.
    // the user is found by id (the username is not unique)
    @Transactional
    public String deleteUser(Long id) {
        UserEntity user = userService.findById(id)
                .orElseThrow(() -> new UserDeletionException("error.user.delete.notfound"));

        // the administrator account cannot be deleted
        if (user.getRole() == Role.ADMIN) {
            throw new UserDeletionException("error.user.delete.admin");
        }

        // TODO when Inscription is in dev: a user only enrolled in modules is deleted directly
        // inscriptionRepository.deleteByUserId(id);

        try {
            userService.delete(user);
        } catch (DataIntegrityViolationException e) {
            // still referenced by another table (responsible of a module, author of a publication...):
            // the database refuses, nothing is deleted
            throw new UserDeletionException("error.user.delete.linked");
        }

        return user.getUsername();
    }

    // the admin switches a teacher between PROF and RESPONSABLE (Responsable Module);
    // returns his username for the success message
    @Transactional
    public String changeRole(Long id, Role newRole) {
        UserEntity user = userService.findById(id)
                .orElseThrow(() -> new InvalidUserDataException("role", "error.user.role.change.notfound"));

        if (newRole == null) {
            throw new InvalidUserDataException("role", "error.user.role.empty");
        }
        // only one administrator
        if (newRole == Role.ADMIN) {
            throw new InvalidUserDataException("role", "error.user.role.change.admin");
        }
        if (user.getRole() == Role.ADMIN) {
            throw new InvalidUserDataException("role", "error.user.role.change.admin.user");
        }
        // only a teacher: a student has a year, a level and a group
        if (!isTeacherRole(user.getRole()) || !isTeacherRole(newRole)) {
            throw new InvalidUserDataException("role", "error.user.role.change.notallowed");
        }
        // a responsable who still manages modules cannot become a simple teacher
        if (user.getRole() == Role.RESPONSABLE && newRole == Role.PROF
                && moduleRepository.existsByResponsibleId(user.getId())) {
            throw new InvalidUserDataException("role", "error.user.role.change.modules");
        }

        if (user.getRole() != newRole) {
            user.setRole(newRole);
            userService.save(user);
        }

        return user.getUsername();
    }

    private boolean isTeacherRole(Role role) {
        return role == Role.PROF || role == Role.RESPONSABLE;
    }

    private void validate(
            String email,
            Role role,
            String username,
            Integer year,
            String level,
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
        // only one administrator: the admin cannot create another one
        if (role == Role.ADMIN) {
            throw new InvalidUserDataException("role", "error.user.role.admin.forbidden");
        }
        if (username == null || username.isBlank()) {
            throw new InvalidUserDataException("username", "error.user.username.empty");
        }
        if (!username.trim().matches(USERNAME_PATTERN)) {
            throw new InvalidUserDataException("username", "error.user.username.invalid");
        }
        // academic year, level and group are required only for students
        // (disabled in the form for the other roles)
        if (role == Role.ETUDIANT) {
            if (year == null) {
                throw new InvalidUserDataException("year", "error.user.year.empty");
            }
            if (year < MIN_YEAR || year > MAX_YEAR) {
                throw new InvalidUserDataException("year", "error.user.year.invalid");
            }
            if (level == null || level.isBlank()) {
                throw new InvalidUserDataException("level", "error.user.level.empty");
            }
            if (!StudyLevel.isValid(level)) {
                throw new InvalidUserDataException("level", "error.user.level.invalid");
            }
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