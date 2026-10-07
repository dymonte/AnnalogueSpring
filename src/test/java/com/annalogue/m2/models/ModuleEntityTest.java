package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}