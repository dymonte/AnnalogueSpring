package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleEntityTest {

    @Test
    void shouldCreateModuleWithName() {
        ModuleEntity module = new ModuleEntity("MOIA");

        assertEquals("MOIA", module.getName());
        // the id is given by the database when the module is saved
        assertNull(module.getId());
    }

    @Test
    void shouldChangeName() {
        ModuleEntity module = new ModuleEntity();
        module.setName("TF");

        assertEquals("TF", module.getName());
    }

    @Test
    public void shouldCreateModuleWithResponsable(){
        UserEntity bfabrice = new UserEntity(
                "fabrice.bouquet@ufc",
                "fake-hash",
                "bfabrice",
                Role.PROF,
                null,
                null
        );
        ModuleEntity module = new ModuleEntity();
        module.setName("TF");
        module.setProf_responsable(bfabrice);

        assertEquals("TF", module.getName());
        assertEquals("bfabrice", module.getProf_responsable().getUsername());

    }


}