package com.annalogue.m2.services;

import com.annalogue.m2.dto.GroupEnrollmentResult;
import com.annalogue.m2.exceptions.ModuleNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotManagedException;
import com.annalogue.m2.models.*;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// TFAN26-106: register all the members of a group in a module
@ExtendWith(MockitoExtension.class)
class InscriptionServiceGroupTest {

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Captor
    private ArgumentCaptor<List<InscriptionEntity>> savedCaptor;

    @InjectMocks
    private InscriptionService inscriptionService;

    private UserEntity responsible;
    private UserEntity otherTeacher;
    private UserEntity anna;
    private UserEntity zoe;
    private ModuleEntity tf;

    @BeforeEach
    void setUp() {
        responsible = new UserEntity("resp@ufr.com", "hash", "resp", Role.RESPONSABLE, null, null);
        responsible.setId(1L);

        otherTeacher = new UserEntity("other@ufr.com", "hash", "other", Role.RESPONSABLE, null, null);
        otherTeacher.setId(2L);

        anna = new UserEntity("anna@ufr.com", "hash", "anna", Role.ETUDIANT, 2026, "G1");
        anna.setId(10L);

        zoe = new UserEntity("zoe@ufr.com", "hash", "zoe", Role.ETUDIANT, 2026, "G1");
        zoe.setId(11L);

        tf = new ModuleEntity("TF", responsible);
        tf.setId(100L);
    }

    @Test
    void shouldRegisterAllMembersOfTheGroup() {
        when(moduleRepository.findById(100L)).thenReturn(Optional.of(tf));
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("G1"))
                .thenReturn(List.of(anna, zoe));
        when(inscriptionRepository.findByModuleId(100L)).thenReturn(List.of());

        GroupEnrollmentResult result = inscriptionService.saveGroup(responsible, "G1", 100L);

        assertEquals(2, result.added());
        assertEquals(0, result.alreadyRegistered());
        assertTrue(result.alreadyRegisteredNames().isEmpty());

        verify(inscriptionRepository).saveAll(savedCaptor.capture());
        List<InscriptionEntity> saved = savedCaptor.getValue();
        assertEquals(2, saved.size());
        for (InscriptionEntity inscription : saved) {
            assertEquals(InscriptionType.ETUDIANT, inscription.getInscriptionType());
            assertEquals(tf, inscription.getModule());
            assertFalse(inscription.isEditor());
        }
        assertEquals(anna, saved.get(0).getUser());
        assertEquals(zoe, saved.get(1).getUser());
    }

    @Test
    void shouldSkipTheMembersAlreadyRegistered() {
        InscriptionEntity annaInTf = new InscriptionEntity(InscriptionType.ETUDIANT, anna, tf);

        when(moduleRepository.findById(100L)).thenReturn(Optional.of(tf));
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("G1"))
                .thenReturn(List.of(anna, zoe));
        when(inscriptionRepository.findByModuleId(100L)).thenReturn(List.of(annaInTf));

        GroupEnrollmentResult result = inscriptionService.saveGroup(responsible, "G1", 100L);

        assertEquals(1, result.added());
        assertEquals(1, result.alreadyRegistered());
        assertEquals(List.of("anna"), result.alreadyRegisteredNames());

        verify(inscriptionRepository).saveAll(savedCaptor.capture());
        assertEquals(1, savedCaptor.getValue().size());
        assertEquals(zoe, savedCaptor.getValue().get(0).getUser());
    }

    @Test
    void shouldAddNobodyWhenWholeGroupIsAlreadyRegistered() {
        when(moduleRepository.findById(100L)).thenReturn(Optional.of(tf));
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("G1"))
                .thenReturn(List.of(anna, zoe));
        when(inscriptionRepository.findByModuleId(100L)).thenReturn(List.of(
                new InscriptionEntity(InscriptionType.ETUDIANT, anna, tf),
                new InscriptionEntity(InscriptionType.ETUDIANT, zoe, tf)));

        GroupEnrollmentResult result = inscriptionService.saveGroup(responsible, "G1", 100L);

        assertEquals(0, result.added());
        assertEquals(2, result.alreadyRegistered());
        assertEquals(List.of("anna", "zoe"), result.alreadyRegisteredNames());
    }

    @Test
    void shouldUseTheEmailWhenAlreadyRegisteredMemberHasNoUsername() {
        anna.setUsername(null);
        InscriptionEntity annaInTf = new InscriptionEntity(InscriptionType.ETUDIANT, anna, tf);

        when(moduleRepository.findById(100L)).thenReturn(Optional.of(tf));
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("G1"))
                .thenReturn(List.of(anna, zoe));
        when(inscriptionRepository.findByModuleId(100L)).thenReturn(List.of(annaInTf));

        GroupEnrollmentResult result = inscriptionService.saveGroup(responsible, "G1", 100L);

        assertEquals(List.of("anna@ufr.com"), result.alreadyRegisteredNames());
    }

    @Test
    void shouldTrimTheGroupName() {
        when(moduleRepository.findById(100L)).thenReturn(Optional.of(tf));
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("G1"))
                .thenReturn(List.of(anna));
        when(inscriptionRepository.findByModuleId(100L)).thenReturn(List.of());

        inscriptionService.saveGroup(responsible, "  G1  ", 100L);

        verify(userRepository).findByGroupNameIgnoreCaseOrderByUsernameAsc("G1");
    }

    @Test
    void shouldRefuseWhenCurrentUserIsMissing() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> inscriptionService.saveGroup(null, "G1", 100L));

        assertEquals("error.inscription.prof.not.found", e.getMessage());
        verifyNoInteractions(moduleRepository, userRepository, inscriptionRepository);
    }

    @Test
    void shouldRefuseWhenModuleIsMissing() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> inscriptionService.saveGroup(responsible, "G1", null));

        assertEquals("error.inscription.module.required", e.getMessage());
        verifyNoInteractions(moduleRepository, userRepository, inscriptionRepository);
    }

    @Test
    void shouldRefuseWhenGroupIsNull() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> inscriptionService.saveGroup(responsible, null, 100L));

        assertEquals("error.inscription.group.required", e.getMessage());
        verifyNoInteractions(moduleRepository, userRepository, inscriptionRepository);
    }

    @Test
    void shouldRefuseWhenGroupIsBlank() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> inscriptionService.saveGroup(responsible, "   ", 100L));

        assertEquals("error.inscription.group.required", e.getMessage());
        verifyNoInteractions(moduleRepository, userRepository, inscriptionRepository);
    }

    @Test
    void shouldRefuseUnknownModule() {
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundException.class,
                () -> inscriptionService.saveGroup(responsible, "G1", 999L));

        verify(inscriptionRepository, never()).saveAll(any());
    }

    @Test
    void shouldRefuseModuleManagedBySomeoneElse() {
        when(moduleRepository.findById(100L)).thenReturn(Optional.of(tf));

        assertThrows(ModuleNotManagedException.class,
                () -> inscriptionService.saveGroup(otherTeacher, "G1", 100L));

        verify(inscriptionRepository, never()).saveAll(any());
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRefuseGroupWithoutMembers() {
        when(moduleRepository.findById(100L)).thenReturn(Optional.of(tf));
        when(userRepository.findByGroupNameIgnoreCaseOrderByUsernameAsc("Z"))
                .thenReturn(List.of());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> inscriptionService.saveGroup(responsible, "Z", 100L));

        assertEquals("error.inscription.group.not.found", e.getMessage());
        verify(inscriptionRepository, never()).saveAll(any());
    }
}