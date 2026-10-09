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
@ActiveProfiles("test") // H2 in-memory database, the real database is never touched
@Transactional          // every change is cancelled after each test
class ModuleRepositoryTest {

    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    private UserRepository userRepository;

    private UserEntity bfabrice;
    @BeforeEach
    public void setUp(){
        bfabrice= new UserEntity(
                "fabrice.bouquet@ufc",
                "fake-hash",
                "bfabrice",
                Role.PROF,
                null,
                null
        );
        userRepository.save(bfabrice);
    }
    @Test
    void shouldSaveModuleAndGiveItAnId() {
        ModuleEntity saved = moduleRepository.save(new ModuleEntity("MOIA", bfabrice));

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindModuleByName() {
        moduleRepository.save(new ModuleEntity("MOIA", bfabrice));

        assertTrue(moduleRepository.findByName("MOIA").isPresent());
        assertTrue(moduleRepository.findByName("TF").isEmpty());
    }

    @Test
    void shouldCheckNameExistsIgnoringCase() {
        moduleRepository.save(new ModuleEntity("MOIA", bfabrice));

        assertTrue(moduleRepository.existsByNameIgnoreCase("moia"));
        assertFalse(moduleRepository.existsByNameIgnoreCase("TF"));
    }

    @Test
    void shouldReturnModulesSortedByName() {
        moduleRepository.save(new ModuleEntity("TF", bfabrice));
        moduleRepository.save(new ModuleEntity("MOIA", bfabrice));

        List<String> names = moduleRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ModuleEntity::getName)
                .toList();

        assertEquals(List.of("MOIA", "TF"), names);
    }

    @Test
    void shouldRejectTwoModulesWithTheSameName() {
        moduleRepository.saveAndFlush(new ModuleEntity("MOIA", bfabrice));

        // the unique constraint of the database refuses the second one
        assertThrows(DataIntegrityViolationException.class,
                () -> moduleRepository.saveAndFlush(new ModuleEntity("MOIA")));
    }

    @Test
    public void shouldThrowExceptionWhenProf_responsableIsNull(){
        ModuleEntity module = new ModuleEntity("PMT");
        assertThrows(DataIntegrityViolationException.class, ()-> moduleRepository.save(module));
    }
}