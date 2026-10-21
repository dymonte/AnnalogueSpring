package com.annalogue.m2.services;

import com.annalogue.m2.dto.UserModuleView;
import com.annalogue.m2.exceptions.InvalidModuleDataException;

import com.annalogue.m2.models.FileType;
import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.InscriptionType;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InscriptionRepository inscriptionRepository;

    @InjectMocks
    private ModuleService moduleService;

    @Mock
    private IUserService iUserService;

    private final UserEntity teacher =
            new UserEntity("fabrice@example.com", "hash", "fabrice", Role.PROF, null, null);

    private void loggedInAs(UserEntity user) {
        when(iUserService.getCurrentUser()).thenReturn(user);
    }


    // ===== Creation =====

    @Test
    void shouldCreateModuleWithTrimmedNameAndResponsible() {
        when(moduleRepository.existsByNameIgnoreCase("MOIA"))
                .thenReturn(false);

        when(moduleRepository.save(any(ModuleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inscriptionRepository.save(any(InscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModuleEntity created =
                moduleService.createModule("  MOIA  ", teacher);

        ArgumentCaptor<ModuleEntity> moduleCaptor =
                ArgumentCaptor.forClass(ModuleEntity.class);

        verify(moduleRepository).save(moduleCaptor.capture());

        assertEquals("MOIA", moduleCaptor.getValue().getName());
        assertEquals(teacher, moduleCaptor.getValue().getResponsible());
        assertEquals("MOIA", created.getName());

        ArgumentCaptor<InscriptionEntity> inscriptionCaptor =
                ArgumentCaptor.forClass(InscriptionEntity.class);

        verify(inscriptionRepository).save(inscriptionCaptor.capture());

        InscriptionEntity inscription = inscriptionCaptor.getValue();

        assertEquals(teacher, inscription.getUser());
        assertEquals(created, inscription.getModule());
        assertEquals(InscriptionType.INTERVENANT,
                inscription.getInscriptionType());
        assertTrue(inscription.isEditor());
    }

    @Test
    void shouldSaveTheModuleOnlyOnce() {
        when(moduleRepository.existsByNameIgnoreCase("MOIA"))
                .thenReturn(false);

        when(moduleRepository.save(any(ModuleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inscriptionRepository.save(any(InscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        moduleService.createModule("MOIA", teacher);

        verify(moduleRepository, times(1))
                .save(any(ModuleEntity.class));

        verify(inscriptionRepository, times(1))
                .save(any(InscriptionEntity.class));
    }

    @Test
    void shouldRejectEmptyName() {
        assertInvalid("error.module.name.empty", "   ");
    }

    @Test
    void shouldRejectNullName() {
        assertInvalid("error.module.name.empty", null);
    }

    @Test
    void shouldRejectTooLongName() {
        assertInvalid("error.module.name.tooLong", "M".repeat(256));
    }

    @Test
    void shouldRejectExistingName() {
        when(moduleRepository.existsByNameIgnoreCase("MOIA"))
                .thenReturn(true);

        assertInvalid("error.module.name.exists", "MOIA");
    }

    private void assertInvalid(String expectedKey, String name) {
        InvalidModuleDataException exception = assertThrows(
                InvalidModuleDataException.class,
                () -> moduleService.createModule(name, teacher)
        );

        assertEquals(expectedKey, exception.getMessageKey());

        verify(moduleRepository, never()).save(any(ModuleEntity.class));
        verify(inscriptionRepository, never())
                .save(any(InscriptionEntity.class));
    }


    // ===== Reading =====

    @Test
    void shouldReturnModulesSortedByName() {
        List<ModuleEntity> modules = List.of(
                new ModuleEntity("MOIA", teacher),
                new ModuleEntity("TF", teacher));
        when(moduleRepository.findAllByOrderByNameAsc()).thenReturn(modules);

        assertEquals(modules, moduleService.findAll());
    }

    @Test
    void shouldFindModuleById() {
        ModuleEntity module = new ModuleEntity("MOIA", teacher);
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

        assertEquals(Optional.of(module), moduleService.findById(1L));
    }

    @Test
    void shouldCheckIfNameExists() {
        when(moduleRepository.existsByNameIgnoreCase("MOIA")).thenReturn(true);

        assertTrue(moduleService.existsByName(" MOIA "));
        assertFalse(moduleService.existsByName(null));
    }


    @Test
    void shouldReturnPublicationsFromModule() {
        ModuleEntity module = new ModuleEntity("MOIA", teacher);
        PublicationEntity pub1 = new PublicationEntity("Pub1", "Desc1", FileType.CM, module, teacher, "/path1");
        PublicationEntity pub2 = new PublicationEntity("Pub2", "Desc2", FileType.TD, module, teacher, "/path2");
        module.setPublications(List.of(pub1, pub2));

        List<PublicationEntity> publications = moduleService.findAllPublicationsFromModule(module);

        assertEquals(2, publications.size());
        assertEquals(pub1, publications.get(0));
        assertEquals(pub2, publications.get(1));
    }

    @Test
    void shouldReturnSameValuesPublicationsFromModule() {
        ModuleEntity module = new ModuleEntity("MOIA", teacher);
        PublicationEntity pub1 = new PublicationEntity("Pub1", "Desc1", FileType.CM, module, teacher, "/path1");
        PublicationEntity pub2 = new PublicationEntity("Pub2", "Desc2", FileType.TD, module, teacher, "/path2");
        module.setPublications(List.of(pub1, pub2));

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

        List<PublicationEntity> publications1 = moduleService.findAllPublicationsFromModule(module);
        List<PublicationEntity> publications2 = moduleService.findAllPublicationsFromModule(1L);

        assertEquals(publications1, publications2);
    }

    @Test
    void shouldReturnEmptyListWhenModuleHasNoPublications() {
        ModuleEntity module = new ModuleEntity("MOIA", teacher);
        module.setPublications(List.of());

        List<PublicationEntity> publications = moduleService.findAllPublicationsFromModule(module);

        assertTrue(publications.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenFindingPublicationsById() {
        List<PublicationEntity> publications = moduleService.findAllPublicationsFromModule(1L);

        assertTrue(publications.isEmpty());
    }

    // ===== My modules (TFAN26-65) =====

    private UserEntity user(long id, String name, Role role) {
        UserEntity user = new UserEntity(name + "@example.com", "hash", name, role, null, null);
        user.setId(id);
        return user;
    }

    private ModuleEntity module(long id, String name, UserEntity responsible) {
        ModuleEntity module = new ModuleEntity(name, responsible);
        module.setId(id);
        return module;
    }

    private List<String> names(List<UserModuleView> modules) {
        return modules.stream().map(UserModuleView::getName).toList();
    }

    @Test
    void studentGetsHisModulesSortedWithHisLabel() {
        UserEntity fabrice = user(1L, "fabrice", Role.PROF);
        UserEntity fabien = user(2L, "fabien", Role.ETUDIANT);
        InscriptionEntity editorInMoia =
                new InscriptionEntity(InscriptionType.ETUDIANT, fabien, module(10L, "MOIA", fabrice));
        editorInMoia.setEditor(true);
        when(userRepository.findByEmail("fabien@example.com")).thenReturn(Optional.of(fabien));
        when(moduleRepository.findByResponsibleIdOrderByNameAsc(2L)).thenReturn(List.of());
        // the repository returns them unsorted: TF before MOIA
        when(inscriptionRepository.findByUserId(2L)).thenReturn(List.of(
                new InscriptionEntity(InscriptionType.ETUDIANT, fabien, module(20L, "TF", fabrice)),
                editorInMoia));

        List<UserModuleView> modules = moduleService.findModulesOfUser("fabien@example.com");

        assertEquals(List.of("MOIA", "TF"), names(modules));
        assertEquals("my.modules.role.ETUDIANT", modules.get(0).getRoleKey());
        assertTrue(modules.get(0).isEditor());
        assertFalse(modules.get(0).isResponsible());
        assertEquals(20L, modules.get(1).getId());
    }

    @Test
    void teacherGetsTheModulesHeCreated() {
        UserEntity fabrice = user(1L, "fabrice", Role.PROF);
        when(userRepository.findByEmail("fabrice@example.com")).thenReturn(Optional.of(fabrice));
        when(moduleRepository.findByResponsibleIdOrderByNameAsc(1L))
                .thenReturn(List.of(module(10L, "MOIA", fabrice), module(20L, "TF", fabrice)));
        when(inscriptionRepository.findByUserId(1L)).thenReturn(List.of());

        List<UserModuleView> modules = moduleService.findModulesOfUser("fabrice@example.com");

        assertEquals(List.of("MOIA", "TF"), names(modules));
        assertTrue(modules.stream().allMatch(UserModuleView::isResponsible));
    }

    @Test
    void createdAndEnrolledModulesAreMergedOnceAndSortedByName() {
        UserEntity fabrice = user(1L, "fabrice", Role.PROF);
        UserEntity claire = user(3L, "claire", Role.PROF);
        ModuleEntity tf = module(20L, "TF", fabrice);
        when(userRepository.findByEmail("fabrice@example.com")).thenReturn(Optional.of(fabrice));
        when(moduleRepository.findByResponsibleIdOrderByNameAsc(1L)).thenReturn(List.of(tf));
        when(inscriptionRepository.findByUserId(1L)).thenReturn(List.of(
                new InscriptionEntity(InscriptionType.INTERVENANT, fabrice, tf),
                new InscriptionEntity(InscriptionType.INTERVENANT, fabrice, module(30L, "JPA", claire))));

        List<UserModuleView> modules = moduleService.findModulesOfUser("fabrice@example.com");

        // TF only once, as « Created by me »; JPA (lecturer) comes first by name
        assertEquals(List.of("JPA", "TF"), names(modules));
        assertEquals("my.modules.role.INTERVENANT", modules.get(0).getRoleKey());
        assertTrue(modules.get(1).isResponsible());
    }

    @Test
    void noModulesForAnUnknownUser() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertTrue(moduleService.findModulesOfUser("ghost@example.com").isEmpty());
        verifyNoInteractions(inscriptionRepository);
    }

    @Test
    void noModulesWithoutEmail() {
        assertTrue(moduleService.findModulesOfUser(null).isEmpty());
        assertTrue(moduleService.findModulesOfUser("  ").isEmpty());

        verifyNoInteractions(userRepository, inscriptionRepository);
    }

    @Test
    void adminViewsContainEveryModuleWithoutLabel() {
        when(moduleRepository.findAllByOrderByNameAsc()).thenReturn(List.of(
                module(10L, "MOIA", teacher), module(20L, "TF", teacher)));

        List<UserModuleView> modules = moduleService.findAllModuleViews();

        assertEquals(List.of("MOIA", "TF"), names(modules));
        assertNull(modules.get(0).getRoleKey());
        assertFalse(modules.get(0).isResponsible());
    }
}