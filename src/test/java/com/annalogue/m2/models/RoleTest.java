package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldHaveCorrectValues() {
        assertEquals("admin", Role.ADMIN.getValue());
        assertEquals("responsable", Role.RESPONSABLE.getValue());
        assertEquals("prof", Role.PROF.getValue());
        assertEquals("etudiant", Role.ETUDIANT.getValue());
    }

    @Test
    void shouldContainAllExpectedRoles() {
        assertEquals(4, Role.values().length);

        assertTrue(java.util.Arrays.asList(Role.values())
                .contains(Role.ADMIN));

        assertTrue(java.util.Arrays.asList(Role.values())
                .contains(Role.RESPONSABLE));

        assertTrue(java.util.Arrays.asList(Role.values())
                .contains(Role.PROF));

        assertTrue(java.util.Arrays.asList(Role.values())
                .contains(Role.ETUDIANT));
    }
}