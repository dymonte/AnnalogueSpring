package com.annalogue.m2.repositories;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;

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
    public void shouldFindAllModulesResponsableUser(){
        ModuleEntity pmt = new ModuleEntity("PMT", bfabrice);
        moduleRepository.save(pmt);
        ModuleEntity tf = new ModuleEntity("TF", bfabrice);
        moduleRepository.save(tf);
        ModuleEntity rx = new ModuleEntity("RX", bfabrice);
        moduleRepository.save(rx);

        bfabrice.getResponsableModules().add(pmt);
        bfabrice.getResponsableModules().add(tf);
        bfabrice.getResponsableModules().add(rx);
        assertEquals(3, bfabrice.getResponsableModules().size());
        assertEquals("PMT",  bfabrice.getResponsableModules().getFirst().getName() );

    }
}

