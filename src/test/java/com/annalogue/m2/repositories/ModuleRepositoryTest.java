package com.annalogue.m2.repositories;

import com.annalogue.m2.models.ModuleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // H2 in-memory database, the real database is never touched
@Transactional          // every change is cancelled after each test
class ModuleRepositoryTest {

    @Autowired
    private ModuleRepository moduleRepository;

    @Test
    void shouldSaveModuleAndGiveItAnId() {
        ModuleEntity saved = moduleRepository.save(new ModuleEntity("MOIA"));

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindModuleByName() {
        moduleRepository.save(new ModuleEntity("MOIA"));

        assertTrue(moduleRepository.findByName("MOIA").isPresent());
        assertTrue(moduleRepository.findByName("TF").isEmpty());
    }

    @Test
    void shouldCheckNameExistsIgnoringCase() {
        moduleRepository.save(new ModuleEntity("MOIA"));

        assertTrue(moduleRepository.existsByNameIgnoreCase("moia"));
        assertFalse(moduleRepository.existsByNameIgnoreCase("TF"));
    }

    @Test
    void shouldReturnModulesSortedByName() {
        moduleRepository.save(new ModuleEntity("TF"));
        moduleRepository.save(new ModuleEntity("MOIA"));

        List<String> names = moduleRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ModuleEntity::getName)
                .toList();

        assertEquals(List.of("MOIA", "TF"), names);
    }

    @Test
    void shouldRejectTwoModulesWithTheSameName() {
        moduleRepository.saveAndFlush(new ModuleEntity("MOIA"));

        // the unique constraint of the database refuses the second one
        assertThrows(DataIntegrityViolationException.class,
                () -> moduleRepository.saveAndFlush(new ModuleEntity("MOIA")));
    }
}