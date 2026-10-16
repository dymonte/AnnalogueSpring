package com.annalogue.m2.repositories;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ModuleRepositoryTest {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity teacher;

    @BeforeEach
    void setUp() {
        teacher = userRepository.save(
                new UserEntity("fabrice@example.com", "hash", "fabrice", Role.PROF, null, null));
    }

    @Test
    void shouldSaveModuleAndGiveItAnId() {
        ModuleEntity saved = moduleRepository.save(new ModuleEntity("MOIA", teacher));

        assertNotNull(saved.getId());
    }

    @Test
    void shouldSaveTheResponsible() {
        moduleRepository.saveAndFlush(new ModuleEntity("MOIA", teacher));

        ModuleEntity found = moduleRepository.findByName("MOIA").orElseThrow();

        assertEquals("fabrice@example.com", found.getResponsible().getEmail());
    }

    @Test
    void shouldFindModuleByName() {
        moduleRepository.save(new ModuleEntity("MOIA", teacher));

        assertTrue(moduleRepository.findByName("MOIA").isPresent());
        assertTrue(moduleRepository.findByName("TF").isEmpty());
    }

    @Test
    void shouldCheckNameExistsIgnoringCase() {
        moduleRepository.save(new ModuleEntity("MOIA", teacher));

        assertTrue(moduleRepository.existsByNameIgnoreCase("moia"));
        assertFalse(moduleRepository.existsByNameIgnoreCase("TF"));
    }

    @Test
    void shouldReturnModulesSortedByName() {

        moduleRepository.save(new ModuleEntity("TF", teacher));
        moduleRepository.save(new ModuleEntity("MOIA", teacher));

        List<String> names = moduleRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ModuleEntity::getName)
                .filter(name -> List.of("TF", "MOIA").contains(name))
                .toList();

        assertEquals(List.of("MOIA", "TF"), names);
    }

    @Test
    void shouldRejectTwoModulesWithTheSameName() {
        moduleRepository.saveAndFlush(new ModuleEntity("MOIA", teacher));

        assertThrows(DataIntegrityViolationException.class,
                () -> moduleRepository.saveAndFlush(new ModuleEntity("MOIA", teacher)));
    }


        @Test
    void shouldFindTheModulesManagedByATeacher() {
        UserEntity claire = userRepository.save(
                new UserEntity("claire@example.com", "hash", "claire", Role.PROF, null, null));
        moduleRepository.save(new ModuleEntity("TF", teacher));
        moduleRepository.save(new ModuleEntity("MOIA", teacher));
        moduleRepository.save(new ModuleEntity("JPA", claire));

        List<String> names = moduleRepository.findByResponsibleIdOrderByNameAsc(teacher.getId())
                .stream()
                .map(ModuleEntity::getName)
                .toList();

        // only Fabrice's modules, sorted; JPA belongs to Claire
        assertEquals(List.of("MOIA", "TF"), names);
    }

    @Test
    void shouldKnowIfAUserStillManagesModules() {
        UserEntity other = userRepository.save(
                new UserEntity("claire@example.com", "hash", "claire", Role.RESPONSABLE, null, null));
        moduleRepository.save(new ModuleEntity("MOIA", teacher));

        assertTrue(moduleRepository.existsByResponsibleId(teacher.getId()));
        assertFalse(moduleRepository.existsByResponsibleId(other.getId()));
    }
}