package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @InjectMocks
    private ModuleService moduleService;

    private final UserEntity teacher =
            new UserEntity("fabrice@example.com", "hash", "fabrice", Role.PROF, null, null);

    private void loggedInAs(UserEntity user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateModuleWithTrimmedNameAndLoggedInUserAsResponsible() {
        loggedInAs(teacher);
        when(moduleRepository.save(any(ModuleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModuleEntity created = moduleService.createModule("  MOIA  ");

        ArgumentCaptor<ModuleEntity> captor = ArgumentCaptor.forClass(ModuleEntity.class);
        verify(moduleRepository).save(captor.capture());

        assertEquals("MOIA", captor.getValue().getName());
        assertEquals(teacher, captor.getValue().getResponsable());
        assertEquals("MOIA", created.getName());
    }

    @Test
    void shouldSaveTheModuleOnlyOnce() {
        loggedInAs(teacher);
        when(moduleRepository.save(any(ModuleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        moduleService.createModule("MOIA");

        verify(moduleRepository, times(1)).save(any(ModuleEntity.class));
    }

    @Test
    void shouldRefuseWhenNobodyIsLoggedIn() {
        assertThrows(IllegalStateException.class, () -> moduleService.createModule("MOIA"));

        verify(moduleRepository, never()).save(any());
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
        when(moduleRepository.existsByNameIgnoreCase("MOIA")).thenReturn(true);

        assertInvalid("error.module.name.exists", "MOIA");
    }

    private void assertInvalid(String expectedKey, String name) {
        InvalidModuleDataException exception = assertThrows(
                InvalidModuleDataException.class,
                () -> moduleService.createModule(name));

        assertEquals(expectedKey, exception.getMessageKey());
        verify(moduleRepository, never()).save(any());
    }

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
}