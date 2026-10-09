package com.annalogue.m2.repositories;

import com.annalogue.m2.models.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, Long> {

    Optional<ModuleEntity> findByName(String name);

    // "TF" and "tf" are considered the same name
    boolean existsByNameIgnoreCase(String name);

    // modules sorted by name, for the lists
    List<ModuleEntity> findAllByOrderByNameAsc();


}