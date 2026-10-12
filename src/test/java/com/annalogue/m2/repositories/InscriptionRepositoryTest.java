
package com.annalogue.m2.repositories;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.InscriptionType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InscriptionRepositoryTest {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity antoine;
    private UserEntity bfabrice;
    private UserEntity fmaiou;
    private UserEntity mamarni;
    private UserEntity ljugo;

    private ModuleEntity tf;
    private ModuleEntity jpa;

    @BeforeEach
    void setUp() {
        antoine = saveUser(
                "antoine.chevlot@test.com",
                "cantoine",
                Role.PROF
        );

        bfabrice = saveUser(
                "fabrice.bouquet@test.com",
                "bfabrice",
                Role.PROF
        );

        fmaiou = saveUser(
                "fmaiou@test.com",
                "fmaiou",
                Role.ETUDIANT
        );

        mamarni = saveUser(
                "mamarni@test.com",
                "mamarni",
                Role.ETUDIANT
        );

        ljugo = saveUser(
                "ljugo@test.com",
                "ljugo",
                Role.ETUDIANT
        );

        tf = saveModule("TF", bfabrice);
        jpa = saveModule("JPA", bfabrice);
    }

    private UserEntity saveUser(
            String email,
            String username,
            Role role
    ) {
        UserEntity user = new UserEntity(
                email,
                "fake-hash",
                username,
                role,
                null,
                null
        );

        return userRepository.save(user);
    }

    private ModuleEntity saveModule(
            String name,
            UserEntity professeur
    ) {
        return moduleRepository.save(new ModuleEntity(name, professeur));
    }

    private InscriptionEntity saveInscription(
            InscriptionType type,
            UserEntity user,
            ModuleEntity module,
            boolean editeur
    ) {
        InscriptionEntity inscription = new InscriptionEntity(
                type,
                user,
                module
        );

        inscription.setEditeur(editeur);

        return inscriptionRepository.save(inscription);
    }

    @Test
    void findByUserId_returnsInscriptionsOfUser() {
        saveInscription(
                InscriptionType.INTERVENANT,
                antoine,
                tf,
                false
        );

        List<InscriptionEntity> result =
                inscriptionRepository.findByUserId(antoine.getId());

        assertEquals(1, result.size());
        assertEquals("TF", result.getFirst().getModule().getName());
        assertEquals(
                InscriptionType.INTERVENANT,
                result.getFirst().getInscriptionType()
        );
        assertEquals(
                "cantoine",
                result.getFirst().getUser().getUsername()
        );
    }

    @Test
    void findByModuleId_returnsOnlyInscriptionsOfRequestedModule() {
        saveInscription(
                InscriptionType.INTERVENANT,
                antoine,
                tf,
                false
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                fmaiou,
                tf,
                false
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                mamarni,
                tf,
                false
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                ljugo,
                jpa,
                false
        );

        List<InscriptionEntity> result =
                inscriptionRepository.findByModuleId(tf.getId());

        assertEquals(3, result.size());
        assertTrue(
                result.stream().allMatch(
                        inscription ->
                                inscription.getModule().getId().equals(tf.getId())
                )
        );
        assertFalse(
                result.stream().anyMatch(
                        inscription ->
                                inscription.getModule().getId().equals(jpa.getId())
                )
        );
    }

    @Test
    void findByInscriptionType_returnsOnlyRequestedType() {
        saveInscription(
                InscriptionType.INTERVENANT,
                antoine,
                tf,
                false
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                fmaiou,
                tf,
                false
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                mamarni,
                tf,
                false
        );

        List<InscriptionEntity> etudiants =
                inscriptionRepository.findByInscriptionType(
                        InscriptionType.ETUDIANT
                );

        List<InscriptionEntity> intervenants =
                inscriptionRepository.findByInscriptionType(
                        InscriptionType.INTERVENANT
                );

        assertEquals(2, etudiants.size());
        assertEquals(1, intervenants.size());

        assertTrue(
                etudiants.stream().allMatch(
                        inscription ->
                                inscription.getInscriptionType()
                                        == InscriptionType.ETUDIANT
                )
        );

        assertTrue(
                intervenants.stream().allMatch(
                        inscription ->
                                inscription.getInscriptionType()
                                        == InscriptionType.INTERVENANT
                )
        );
    }

    @Test
    void findByEditeurTrue_returnsOnlyEditors() {
        saveInscription(
                InscriptionType.INTERVENANT,
                antoine,
                tf,
                true
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                fmaiou,
                tf,
                false
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                mamarni,
                tf,
                false
        );

        List<InscriptionEntity> result =
                inscriptionRepository.findByEditeurTrue();

        assertEquals(1, result.size());
        assertTrue(
                result.stream().allMatch(InscriptionEntity::isEditeur)
        );
        assertEquals(
                "cantoine",
                result.getFirst().getUser().getUsername()
        );
    }

    @Test
    void findByEditeurFalse_returnsOnlyNonEditors() {
        saveInscription(
                InscriptionType.INTERVENANT,
                antoine,
                tf,
                true
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                fmaiou,
                tf,
                false
        );

        saveInscription(
                InscriptionType.ETUDIANT,
                mamarni,
                tf,
                false
        );

        List<InscriptionEntity> result =
                inscriptionRepository.findByEditeurFalse();

        assertEquals(2, result.size());
        assertTrue(
                result.stream().noneMatch(InscriptionEntity::isEditeur)
        );
        assertTrue(
                result.stream().anyMatch(
                        inscription ->
                                inscription.getUser().getUsername().equals("fmaiou")
                )
        );
        assertTrue(
                result.stream().anyMatch(
                        inscription ->
                                inscription.getUser().getUsername().equals("mamarni")
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {

        assertThrows(DataIntegrityViolationException.class, () -> saveInscription(InscriptionType.ETUDIANT, null,tf ,false));
    }

    @Test
    void shouldThrowExceptionWhenModuleIsNull() {

        assertThrows(DataIntegrityViolationException.class, () -> saveInscription(InscriptionType.ETUDIANT, fmaiou, null, false ));
    }

    @Test
    void shouldThrowExceptionWhenInscriptionTypeIsNull() {

        assertThrows(DataIntegrityViolationException.class, () -> saveInscription(null, fmaiou, tf, false ));
    }

    @Test
    void shouldThrowExceptionWhenUser_ModuleDuplicated(){

        assertThrows(DataIntegrityViolationException.class, () ->{
            saveInscription(InscriptionType.ETUDIANT, fmaiou, tf, false );
            saveInscription(InscriptionType.INTERVENANT, fmaiou, tf, true);
        });



    }

}
