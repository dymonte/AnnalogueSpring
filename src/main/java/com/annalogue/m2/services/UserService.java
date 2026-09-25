package com.annalogue.m2.services;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Optional<UserEntity> findByIdentifiant(String identifiant) {
        return userRepository.findByIdentifiant(identifiant);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserEntity save(UserEntity user) {
        user.setHashMdp(passwordEncoder.encode(user.getHashMdp()));
        return userRepository.save(user);
    }
}