package com.annalogue.m2.ldap; // A changer si besoin

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.apache.catalina.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.annalogue.m2.models.StudyLevel;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModuleRepository moduleRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, ModuleRepository moduleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.moduleRepository = moduleRepository;
    }

    private void createUser(String email, String username, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            System.out.println(">>> USER: " + email + " already exist, SKIP");
            return;
        }

        userRepository.save(
                new UserEntity(email, passwordEncoder.encode(password), username, role, null, null)
        );

        System.out.println(">>> USER: " + username + ", " + email + ", " + role.getValue() + ", created");
    }

    private void createModule(String name) {
        if (moduleRepository.existsByNameIgnoreCase(name)) return;

        moduleRepository.save(
                new ModuleEntity(name, userRepository.findByEmail("resp@example.com").get())
        );
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> DataInitializer démarré");

        long user_count = userRepository.count();
        System.out.println(">>> Nombre d'utilisateurs : " + user_count);

        System.out.println(">>> Création des comptes de test");
        createUser("admin@example.com", "admin", "admin123", Role.ADMIN);
        createUser("resp@example.com", "resp", "resp1234", Role.RESPONSABLE);
        createUser("user1@example.com", "user1", "user1123", Role.ETUDIANT);
        createUser("prof@example.com", "prof", "prof123", Role.PROF);

        UserEntity user1 = userRepository.findByEmail("user1@example.com").get();
        user1.setYear(2024);
        user1.setLevel(StudyLevel.L1);
        user1.setGroupName("A");
        userRepository.save(user1);

        createModule("test_module");
    }
}