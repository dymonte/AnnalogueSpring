package com.annalogue.m2.services;

import com.annalogue.m2.dto.UserModuleView;
import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ModuleService implements IModuleService {

    private static final int MAX_NAME_LENGTH = 255;
    private static final String ROLE_KEY_PREFIX = "my.modules.role.";

    private final ModuleRepository moduleRepository;

    private final UserRepository userRepository;
    private final IUserService iUserService;
    private final InscriptionRepository inscriptionRepository;

    public ModuleService(ModuleRepository moduleRepository,
                         UserRepository userRepository,
                         InscriptionRepository inscriptionRepository,
                         IUserService iUserService) {

        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.iUserService = iUserService;
        this.inscriptionRepository = inscriptionRepository;
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
    public List<UserModuleView> findModulesOfUser(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        Optional<UserEntity> user = userRepository.findByEmail(email.trim());
        if (user.isEmpty()) {
            return List.of();
        }
        Long userId = user.get().getId();

        Map<Long, UserModuleView> modules = new LinkedHashMap<>();

        for (ModuleEntity module : moduleRepository.findByResponsibleIdOrderByNameAsc(userId)) {
            modules.put(module.getId(), new UserModuleView(
                    module.getId(), module.getName(), UserModuleView.RESPONSIBLE_ROLE_KEY, false));
        }

        for (InscriptionEntity inscription : inscriptionRepository.findByUserId(userId)) {
            ModuleEntity module = inscription.getModule();
            modules.putIfAbsent(module.getId(), new UserModuleView(
                    module.getId(),
                    module.getName(),
                    ROLE_KEY_PREFIX + inscription.getInscriptionType().name(),
                    inscription.isEditor()));
        }

        return modules.values().stream()
                .sorted(Comparator.comparing(UserModuleView::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<UserModuleView> findAllModuleViews() {
        return findAll().stream()
                .map(module -> new UserModuleView(module.getId(), module.getName(), null, false))
                .toList();
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

    @Override
    public void deleteAll() {
        moduleRepository.deleteAll();
    }
}