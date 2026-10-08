package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.repositories.ModuleRepository;
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

    @InjectMocks
    private ModuleService moduleService;

    // ===== Creation =====

    @Test
    void shouldCreateModuleWithTrimmedName() {
        when(moduleRepository.save(any(ModuleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModuleEntity created = moduleService.createModule("  MOIA  ");

        ArgumentCaptor<ModuleEntity> captor = ArgumentCaptor.forClass(ModuleEntity.class);
        verify(moduleRepository).save(captor.capture());

        assertEquals("MOIA", captor.getValue().getName());
        assertEquals("MOIA", created.getName());
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

    // ===== Reading =====

    @Test
    void shouldReturnModulesSortedByName() {
        List<ModuleEntity> modules = List.of(new ModuleEntity("MOIA"), new ModuleEntity("TF"));
        when(moduleRepository.findAllByOrderByNameAsc()).thenReturn(modules);

        assertEquals(modules, moduleService.findAll());
    }

    @Test
    void shouldFindModuleById() {
        ModuleEntity module = new ModuleEntity("MOIA");
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