package com.annalogue.m2.services;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    // TODO: complete

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}

