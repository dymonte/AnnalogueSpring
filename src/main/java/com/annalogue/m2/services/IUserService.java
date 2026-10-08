package com.annalogue.m2.services;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    public Optional<UserEntity> findByEmail(String email);
    public Optional<UserEntity> findByUsername(String username);
    public boolean existsByEmail(String email);
    public UserEntity save(UserEntity user);
    public List<UserEntity> findByRole(Role role);
    public void deleteAll();
    public boolean existsByUsername(String username);
}
