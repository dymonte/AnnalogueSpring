        package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InscriptionEntityTest {

    @Test
    void shouldCreateEtuInscriptionEntity() {
        UserEntity bfabrice = createProfResponsable();
        UserEntity fmaiou = new UserEntity(
                "fadila.maiou@test.com",
                "fake-hash",
                "fmaiou",
                Role.ETUDIANT,
                2026,
                "G1"
        );

        ModuleEntity tf = new ModuleEntity("TF",bfabrice);

        InscriptionEntity inscription = new InscriptionEntity(
                InscriptionType.ETUDIANT,
                fmaiou,
                tf
        );

        assertEquals(
                InscriptionType.ETUDIANT,
                inscription.getInscriptionType()
        );
        assertEquals(
                "fmaiou",
                inscription.getUser().getUsername()
        );
        assertEquals(
                "TF",
                inscription.getModule().getName()
        );
        assertFalse(inscription.isEditeur());

        assertEquals(
                "bfabrice",
                inscription.getModule()
                        .getProf_responsable()
                        .getUsername()
        );
    }

    @Test
    void shouldCreateIntervInscriptionEntity() {

        UserEntity bfabrice = createProfResponsable();
        UserEntity antoine = new UserEntity(
                "antoine.chevlot@test.com",
                "fake-hash",
                "cantoine",
                Role.PROF,
                null,
                null
        );
        ModuleEntity tf = new ModuleEntity("TF", bfabrice);

        InscriptionEntity inscription = new InscriptionEntity(
                InscriptionType.INTERVENANT,
                antoine,
                tf
        );

        assertEquals(
                InscriptionType.INTERVENANT,
                inscription.getInscriptionType()
        );
        assertEquals(
                "cantoine",
                inscription.getUser().getUsername()
        );
        assertEquals(
                "TF",
                inscription.getModule().getName()
        );
        assertFalse(inscription.isEditeur());

        assertEquals(
                "bfabrice",
                inscription.getModule()
                        .getProf_responsable()
                        .getUsername()
        );

        inscription.setEditeur(true);

        assertTrue(inscription.isEditeur());
    }

    UserEntity createProfResponsable(){
        return new UserEntity(
                "fabrice.bouquet@test.com",
                "fake-hash",
                "bfabrice",
                Role.PROF,
                null,
                null
        );
    }
}
