package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ModuleEntityTest {

    private final UserEntity teacher =
            new UserEntity("fabrice@example.com", "hash", "fabrice", Role.PROF, null, null);

    @Test
    void shouldCreateModuleWithNameAndResponsible() {
        ModuleEntity module = new ModuleEntity("MOIA", teacher);

        assertEquals("MOIA", module.getName());
        assertEquals(teacher, module.getResponsible());
        assertNull(module.getId());
    }

    @Test
    void shouldChangeName() {
        ModuleEntity module = new ModuleEntity();
        module.setName("TF");

        assertEquals("TF", module.getName());
    }

    @Test
    void shouldChangeResponsible() {
        UserEntity otherTeacher =
                new UserEntity("claire@example.com", "hash", "claire", Role.PROF, null, null);
        ModuleEntity module = new ModuleEntity("MOIA", teacher);

        module.setResponsible(otherTeacher);

        assertEquals(otherTeacher, module.getResponsible());
    }
}