package com.annalogue.m2.ldap; // A changer si besoin

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
        System.out.println(">>> DataInitializer démarré");

        long count = userRepository.count();
        System.out.println(">>> Nombre d'utilisateurs : " + count);

        if (count == 0) {
            System.out.println(">>> Création des comptes de test");

            UserEntity admin = new UserEntity();
            admin.setEmail("admin@example.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setUsername("admin");
            admin.setRole(Role.ADMIN);
            admin.setYear(2024);
            admin.setGroupName("A");
            userRepository.save(admin);

            UserEntity user1 = new UserEntity();
            user1.setEmail("user1@example.com");
            user1.setPasswordHash(passwordEncoder.encode("user1123"));
            user1.setUsername("user1");
            user1.setRole(Role.ETUDIANT);
            user1.setYear(2024);
            user1.setGroupName("A");
            userRepository.save(user1);

            System.out.println(">>> Comptes créés !");
        } else {
            System.out.println(">>> Initialisation ignorée : la table n'est pas vide");
        }
    }
}