package com.annalogue.m2.repositories;

import com.annalogue.m2.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    // the username is not unique (only a warning in the form): several users can
    // share it
    List<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    // members list, sorted by username
    List<UserEntity> findAllByOrderByUsernameAsc();

    List<UserEntity> findByGroupNameIgnoreCaseOrderByUsernameAsc(String groupName);
}