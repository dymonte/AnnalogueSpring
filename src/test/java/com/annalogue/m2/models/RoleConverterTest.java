package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleConverterTest {

    private final RoleConverter converter = new RoleConverter();

    @Test
    void shouldConvertRoleToDatabaseValue() {
        assertEquals("admin", converter.convertToDatabaseColumn(Role.ADMIN));
        assertEquals("prof", converter.convertToDatabaseColumn(Role.PROF));
        assertEquals("étudiant", converter.convertToDatabaseColumn(Role.ETUDIANT));
    }

    @Test
    void shouldConvertDatabaseValueToRole() {
        assertEquals(Role.ADMIN, converter.convertToEntityAttribute("admin"));
        assertEquals(Role.PROF, converter.convertToEntityAttribute("prof"));
        assertEquals(Role.ETUDIANT, converter.convertToEntityAttribute("étudiant"));
    }

    @Test
    void shouldReturnNullWhenRoleIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void shouldReturnNullWhenDatabaseValueIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void shouldThrowExceptionForUnknownRole() {
        assertThrows(
                IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("unknown"));
    }
}