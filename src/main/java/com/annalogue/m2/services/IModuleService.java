package com.annalogue.m2.services;

import com.annalogue.m2.dto.UserModuleView;
import com.annalogue.m2.models.ModuleEntity;

import java.util.List;
import java.util.Optional;

public interface IModuleService {

    // creates a module; throws InvalidModuleDataException if the name is empty, too long or already used
    public ModuleEntity createModule(String name);

    // all modules, sorted by name
    public List<ModuleEntity> findAll();

    public Optional<ModuleEntity> findById(Long id);

    public boolean existsByName(String name);

    public void deleteAll();

    public List<UserModuleView> findModulesOfUser(String email);

    public List<UserModuleView> findAllModuleViews();

}