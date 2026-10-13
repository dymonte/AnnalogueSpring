package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService implements IModuleService {

    private static final int MAX_NAME_LENGTH = 255;

    private final ModuleRepository moduleRepository;

    private final UserRepository userRepository;
    private final IUserService iUserService;


    public ModuleService(ModuleRepository moduleRepository, UserRepository userRepository, IUserService iuserService) {
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.iUserService = iuserService;
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

        UserEntity currentUser = iUserService.getCurrentUser();

        return moduleRepository.save(new ModuleEntity(moduleName, currentUser));
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