package com.annalogue.m2.services;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserEntity save(UserEntity user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public List<UserEntity> findByRole(Role role) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
}