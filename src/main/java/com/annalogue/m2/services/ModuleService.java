package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ModuleService implements IModuleService {

    private static final int MAX_NAME_LENGTH = 255;

    private final ModuleRepository moduleRepository;

    private final UserRepository userRepository;

    public ModuleService(ModuleRepository moduleRepository, UserRepository userRepository) {
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
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

        UserEntity currentUser = getCurrentUser();

        return moduleRepository.save(new ModuleEntity(moduleName, currentUser));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            throw new IllegalStateException("No users logged in");
        }

        return authentication.getName();
    }

    private UserEntity getCurrentUser() {
        String email = getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
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