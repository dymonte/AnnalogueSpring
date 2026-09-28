package com.annalogue.m2;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            UserEntity admin = new UserEntity();
            admin.setEmail("admin@example.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setUsername("admin");
            admin.setRole(Role.ADMIN);
            admin.setYear(2024);
            admin.setGroupName("A");

            userRepository.save(admin);
            System.out.println("Utilisateur admin créé: admin@example.com / admin123");

            UserEntity user1 = new UserEntity();
            user1.setEmail("user1@example.com");
            user1.setPasswordHash(passwordEncoder.encode("user1123"));
            user1.setUsername("user1");
            user1.setRole(Role.ETUDIANT);
            user1.setYear(2024);
            user1.setGroupName("A");

            userRepository.save(user1);
            System.out.println("Utilisateur user1 créé: user1@example.com / user1123");
        }
    }
}