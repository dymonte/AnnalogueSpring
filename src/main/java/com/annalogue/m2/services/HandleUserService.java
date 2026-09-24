package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.EmailAlreadyExistsException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HandleUserService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public HandleUserService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public String addUser(String email, Role role, String annee, String groupe) {
        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Account with this email already exists");
        }

        String temporaryPassword = generateTemporaryPassword();
        String hashMdp = passwordEncoder.encode(temporaryPassword);
        String identifiant = generateIdentifiant(email);

        UserEntity user = new UserEntity(email, hashMdp, identifiant, role, annee, groupe);
        userService.save(user);
        // Here you would typically call the EmailService to send the welcome email


        return temporaryPassword;
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // abdelmadjid.kahoul@univ.fr -> akahoul exemple  like  moodle account 
    private String generateIdentifiant(String email) {
        String name = email.substring(0, email.indexOf("@"));

        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            return (parts[0].charAt(0) + parts[1]).toLowerCase();
        }

        return name.toLowerCase();
    }
}