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
        assertEquals("fake-hash", user.getHashMdp());
        assertEquals("test123", user.getIdentifiant());
        assertEquals(Role.ETUDIANT, user.getRole());
        assertEquals(2026, user.getAnnee());
        assertEquals("G1", user.getGroupe());
    }

    @Test
    void shouldSetAndGetAllFields() {

        UserEntity user = new UserEntity();

        user.setEmail("new@test.com");
        user.setHashMdp("new-hash");
        user.setIdentifiant("new-id");
        user.setRole(Role.PROF);
        user.setAnnee(2025);
        user.setGroupe("G2");

        assertEquals("new@test.com", user.getEmail());
        assertEquals("new-hash", user.getHashMdp());
        assertEquals("new-id", user.getIdentifiant());
        assertEquals(Role.PROF, user.getRole());
        assertEquals(2025, user.getAnnee());
        assertEquals("G2", user.getGroupe());
    }
}