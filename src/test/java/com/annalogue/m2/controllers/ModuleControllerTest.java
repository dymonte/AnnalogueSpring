package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ModuleControllerTest {

    private final IModuleService moduleService = mock(IModuleService.class);

    private final UserEntity teacher =
            new UserEntity("fabrice@example.com", "hash", "fabrice", Role.PROF, null, null);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ModuleController(moduleService))
                .build();
    }

    @Test
    void shouldShowAddForm() throws Exception {
        mockMvc.perform(get("/modules/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"));
    }

    @Test
    void shouldAddModuleAndRedirectWithSuccessMessage() throws Exception {
        when(moduleService.createModule("MOIA")).thenReturn(new ModuleEntity("MOIA", teacher));

        mockMvc.perform(post("/modules/add").param("name", "MOIA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/modules"))
                .andExpect(flash().attribute("successKey", "success.module.created"))
                .andExpect(flash().attribute("createdModuleName", "MOIA"));

        verify(moduleService).createModule("MOIA");
    }

    @Test
    void shouldShowErrorWhenNameIsEmpty() throws Exception {
        when(moduleService.createModule(any()))
                .thenThrow(new InvalidModuleDataException("error.module.name.empty"));

        mockMvc.perform(post("/modules/add").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("nameErrorKey", "error.module.name.empty"));
    }

    @Test
    void shouldShowErrorWhenNameIsMissing() throws Exception {
        when(moduleService.createModule(null))
                .thenThrow(new InvalidModuleDataException("error.module.name.empty"));

        mockMvc.perform(post("/modules/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("nameErrorKey", "error.module.name.empty"));
    }

    @Test
    void shouldShowErrorAndKeepNameWhenNameAlreadyExists() throws Exception {
        when(moduleService.createModule("MOIA"))
                .thenThrow(new InvalidModuleDataException("error.module.name.exists"));

        mockMvc.perform(post("/modules/add").param("name", "MOIA"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("nameErrorKey", "error.module.name.exists"))
                .andExpect(model().attribute("name", "MOIA"));
    }

    @Test
    void shouldShowErrorWhenNameIsTooLong() throws Exception {
        String longName = "M".repeat(256);
        when(moduleService.createModule(longName))
                .thenThrow(new InvalidModuleDataException("error.module.name.tooLong"));

        mockMvc.perform(post("/modules/add").param("name", longName))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("nameErrorKey", "error.module.name.tooLong"));
    }

    @Test
    void shouldShowDatabaseErrorWhenSavingFails() throws Exception {
        when(moduleService.createModule("MOIA"))
                .thenThrow(new DataAccessResourceFailureException("Database down"));

        mockMvc.perform(post("/modules/add").param("name", "MOIA"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("errorKey", "error.module.database"))
                .andExpect(model().attribute("name", "MOIA"));
    }

    @Test
    void shouldListModules() throws Exception {
        List<ModuleEntity> modules = List.of(
                new ModuleEntity("MOIA", teacher),
                new ModuleEntity("TF", teacher));
        when(moduleService.findAll()).thenReturn(modules);

        mockMvc.perform(get("/modules"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"))
                .andExpect(model().attribute("modules", modules));
    }

    @Test
    void shouldShowModulePage() throws Exception {
        ModuleEntity module = new ModuleEntity("MOIA", teacher);
        when(moduleService.findById(1L)).thenReturn(Optional.of(module));

        mockMvc.perform(get("/modules/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/detail"))
                .andExpect(model().attribute("module", module));
    }

    @Test
    void shouldReturn404WhenModuleDoesNotExist() throws Exception {
        when(moduleService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/modules/99"))
                .andExpect(status().isNotFound());
    }
}