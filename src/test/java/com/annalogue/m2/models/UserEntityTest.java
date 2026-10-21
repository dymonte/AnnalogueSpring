package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void shouldCreateUserWithConstructor() {

        UserEntity user = new UserEntity(
                "test@test.com",
                "fake-hash",
                "test123",
                Role.ETUDIANT,
                2026,
                "G1");

        assertEquals("test@test.com", user.getEmail());
        assertEquals("fake-hash", user.getPasswordHash());
        assertEquals("test123", user.getUsername());
        assertEquals(Role.ETUDIANT, user.getRole());
        assertEquals(2026, user.getYear());
        assertEquals("G1", user.getGroupName());
    }

    @Test
    void shouldSetAndGetAllFields() {

        UserEntity user = new UserEntity();

        user.setEmail("new@test.com");
        user.setPasswordHash("new-hash");
        user.setUsername("new-id");
        user.setRole(Role.PROF);
        user.setYear(2025);
        user.setGroupName("G2");

        assertEquals("new@test.com", user.getEmail());
        assertEquals("new-hash", user.getPasswordHash());
        assertEquals("new-id", user.getUsername());
        assertEquals(Role.PROF, user.getRole());
        assertEquals(2025, user.getYear());
        assertEquals("G2", user.getGroupName());
    }

        @Test
    void shouldSetAndGetLevel() {

        UserEntity user = new UserEntity();
        user.setLevel(StudyLevel.M1);

        assertEquals(StudyLevel.M1, user.getLevel());
    }

    @Test
    void shouldHaveNotificationsOnByDefault() {
        UserEntity user = new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, 2026, "A");

        assertTrue(user.isNotificationsEnabled());
    }

    @Test
    void shouldTurnNotificationsOffAndOn() {
        UserEntity user = new UserEntity();

        user.setNotificationsEnabled(false);
        assertFalse(user.isNotificationsEnabled());

        user.setNotificationsEnabled(true);
        assertTrue(user.isNotificationsEnabled());
    }
}