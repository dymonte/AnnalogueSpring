package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.EmailNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotManagedException;
import com.annalogue.m2.exceptions.UserAlreadyEnrolledException;
import com.annalogue.m2.models.*;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscriptionServiceTest {

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private InscriptionService inscriptionService;

    private UserEntity fmaiou;
    private UserEntity achevlot;
    private UserEntity fbouquet;

    private ModuleEntity tf;
    private ModuleEntity java;
    private ModuleEntity rx;
    private InscriptionEntity tf_fmaiou;
    private InscriptionEntity tf_achevlot;
    private InscriptionEntity rx_fmaiou;
    @BeforeEach
    void setUp() {
        fmaiou = new UserEntity(
                "fmaiou@ufr.com",
                "fake-hash",
                "fmaiou",
                Role.ETUDIANT,
                2026,
                "G1"
        );
        fmaiou.setId(2L);

        achevlot = new UserEntity(
                "achevlot@ufr.com",
                "fake-hash",
                "achevlot",
                Role.PROF,
                null,
                null
        );
        achevlot.setId(3L);

        fbouquet = new UserEntity(
                "fbouquet@ufr.com",
                "fake-hash",
                "fbouquet",
                Role.PROF,
                null,
                null
        );
        fbouquet.setId(1L);

        tf = new ModuleEntity("TF", fbouquet);
        tf.setId(10L);

        java = new ModuleEntity("Java", fbouquet);
        java.setId(11L);

        rx = new ModuleEntity("RX", achevlot);
        rx.setId(12L);

        achevlot.setResponsibleModules(List.of(rx));
        fbouquet.setResponsibleModules(List.of(tf, java));

        tf_fmaiou = new InscriptionEntity(InscriptionType.ETUDIANT, fmaiou, tf);tf_fmaiou.setId(1L);
        tf_achevlot = new InscriptionEntity(InscriptionType.INTERVENANT, achevlot, tf);tf_achevlot.setId(2L);
        rx_fmaiou = new InscriptionEntity(InscriptionType.ETUDIANT, fmaiou, rx);rx_fmaiou.setId(3L);
    }

    @Test
    void shouldRegisterEtudiantInscription() {


        when(userRepository.findByEmail("fmaiou@ufr.com"))
                .thenReturn(Optional.of(fmaiou));

        when(moduleRepository.findById(10L))
                .thenReturn(Optional.of(tf));

        when(inscriptionRepository.findByUserId(2L))
                .thenReturn(List.of());

        when(inscriptionRepository.save(any(InscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));



        InscriptionEntity inscription = inscriptionService.save(
                fbouquet,
                "fmaiou@ufr.com",
                10L
        );


        assertNotNull(inscription);

        assertEquals(
                InscriptionType.ETUDIANT,
                inscription.getInscriptionType()
        );

        assertEquals(
                "TF",
                inscription.getModule().getName()
        );

        assertEquals(
                "fmaiou@ufr.com",
                inscription.getUser().getEmail()
        );

        assertFalse(inscription.isEditor());


        verify(inscriptionRepository)
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldRegisterProfAsIntervenant() {
        when(userRepository.findByEmail("achevlot@ufr.com"))
                .thenReturn(Optional.of(achevlot));

        when(moduleRepository.findById(10L))
                .thenReturn(Optional.of(tf));

        when(inscriptionRepository.findByUserId(3L))
                .thenReturn(List.of());

        when(inscriptionRepository.save(any(InscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscriptionEntity inscription = inscriptionService.save(
                fbouquet,
                "achevlot@ufr.com",
                10L
        );

        assertEquals(
                InscriptionType.INTERVENANT,
                inscription.getInscriptionType()
        );
    }

    @Test
    void shouldRegisterResponsableOfAnotherModuleAsIntervenant() {
        UserEntity claire = new UserEntity("claire@ufr.com", "fake-hash", "claire", Role.RESPONSABLE, null, null);
        claire.setId(4L);

        when(userRepository.findByEmail("claire@ufr.com"))
                .thenReturn(Optional.of(claire));

        when(moduleRepository.findById(10L))
                .thenReturn(Optional.of(tf));

        when(inscriptionRepository.findByUserId(4L))
                .thenReturn(List.of());

        when(inscriptionRepository.save(any(InscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscriptionEntity inscription = inscriptionService.save(
                fbouquet,
                "claire@ufr.com",
                10L
        );

        assertEquals(
                InscriptionType.INTERVENANT,
                inscription.getInscriptionType()
        );
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByEmail("fmaiou@ufr.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                EmailNotFoundException.class,
                () -> inscriptionService.save(
                        fbouquet,
                        "fmaiou@ufr.com",
                        10L
                )
        );

        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenModuleNotFound() {

        when(userRepository.findByEmail("fmaiou@ufr.com"))
                .thenReturn(Optional.of(fmaiou));

        when(moduleRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ModuleNotFoundException.class,
                () -> inscriptionService.save(
                        fbouquet,
                        "fmaiou@ufr.com",
                        10L
                )
        );

        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenModuleNotMangedByCurrentUserFound() {

        when(userRepository.findByEmail("fmaiou@ufr.com"))
                .thenReturn(Optional.of(fmaiou));

        when(moduleRepository.findById(10L))
                .thenReturn(Optional.of(tf));

        assertThrows(
                ModuleNotManagedException.class,
                () -> inscriptionService.save(
                        achevlot,
                        "fmaiou@ufr.com",
                        10L
                )
        );

        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyEnrolled() {

        when(userRepository.findByEmail("fmaiou@ufr.com"))
                .thenReturn(Optional.of(fmaiou));

        when(moduleRepository.findById(10L))
                .thenReturn(Optional.of(tf));

        InscriptionEntity Inscription = new InscriptionEntity();
        Inscription.setUser(fmaiou);
        Inscription.setModule(tf);
        Inscription.setInscriptionType(InscriptionType.ETUDIANT);

        when(inscriptionRepository.findByUserId(2L))
                .thenReturn(List.of(Inscription));

        assertThrows(
                UserAlreadyEnrolledException.class,
                () -> inscriptionService.save(
                        fbouquet,
                        "fmaiou@ufr.com",
                        10L
                )
        );

        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenCurrentUserIsNull(){
        IllegalArgumentException exception=assertThrows(
                IllegalArgumentException.class,
                ()->inscriptionService.save(
                        null,
                        "fmaiou@ufr.com",
                        10L
                )
        );

        assertEquals("error.inscription.prof.not.found", exception.getMessage());
        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenModuleIdIsNull(){
        IllegalArgumentException exception=assertThrows(
                IllegalArgumentException.class,
                ()->inscriptionService.save(
                        fbouquet,
                        "fmaiou@ufr.com",
                        null
                )
        );

        assertEquals("error.inscription.module.required", exception.getMessage());
        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserMailIsNull(){
        IllegalArgumentException exception=assertThrows(
                IllegalArgumentException.class,
                ()->inscriptionService.save(
                        fbouquet,
                        null,
                        10L
                )
        );

        assertEquals("error.inscription.email.required", exception.getMessage());
        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserMailInvalid(){
        IllegalArgumentException exception=assertThrows(
                IllegalArgumentException.class,
                ()->inscriptionService.save(
                        fbouquet,
                        "fmaiou1125",
                        10L
                )
        );

        assertEquals("error.inscription.email.invalid", exception.getMessage());
        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserMailIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inscriptionService.save(
                        fbouquet,
                        "   ",
                        10L
                )
        );

        assertEquals(
                "error.inscription.email.required",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository, moduleRepository, inscriptionRepository);
    }

    @Test
    void shouldFindInscriptionsModulesByUserEntity(){
        when(inscriptionService.findByModuleId(tf.getId())).thenReturn(
                List.of(tf_fmaiou, tf_achevlot)
        );
        when(inscriptionService.findByModuleId(java.getId())).thenReturn(
                List.of()
        );

        List<InscriptionEntity> inscrptions = inscriptionService.findInscriptions(fbouquet);
        assertEquals(List.of(tf_fmaiou, tf_achevlot), inscrptions);
        assertEquals(2, inscrptions.size());
        for (InscriptionEntity inscrption : inscrptions) {
            assertEquals(fbouquet, inscrption.getModule().getResponsible());
            assertNotEquals(rx.getName(),inscrption.getModule().getName());
        }

        assertFalse(inscrptions.contains(rx_fmaiou));
        fbouquet.setResponsibleModules(List.of());
        inscrptions = inscriptionService.findInscriptions(fbouquet);

        assertTrue(inscrptions.isEmpty());


    }

    @Test
    void shouldToggleEditor(){
        when(inscriptionService.findById(1L)).thenReturn(Optional.of(tf_fmaiou));

        Optional<InscriptionEntity> inscription = inscriptionService.findById(1L);
        assertTrue(inscription.isPresent());

        assertFalse(inscription.get().isEditor());
        boolean editor = inscriptionService.toggleEditor(1L);

        assertTrue(inscription.get().isEditor());

        editor = inscriptionService.toggleEditor(1L);
        assertFalse(inscription.get().isEditor());



    }



}