package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService implements IModuleService {

    private static final int MAX_NAME_LENGTH = 255;

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Override
    @Transactional
    public ModuleEntity createModule(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidModuleDataException("error.module.name.empty");
        }

        String moduleName = name.trim();

        if (moduleName.length() > MAX_NAME_LENGTH) {
            throw new InvalidModuleDataException("error.module.name.tooLong");
        }
        if (moduleRepository.existsByNameIgnoreCase(moduleName)) {
            throw new InvalidModuleDataException("error.module.name.exists");
        }

        return moduleRepository.save(new ModuleEntity(moduleName));
    }

    @Override
    public List<ModuleEntity> findAll() {
        return moduleRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Optional<ModuleEntity> findById(Long id) {
        return moduleRepository.findById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return name != null && moduleRepository.existsByNameIgnoreCase(name.trim());
    }
}