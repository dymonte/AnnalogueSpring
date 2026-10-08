package com.annalogue.m2.controllers;

import com.annalogue.m2.services.IModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.annalogue.m2.models.ModuleEntity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.mockito.ArgumentMatchers.any;
import java.util.List;

class ModuleControllerTest {

    private final IModuleService moduleService = mock(IModuleService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // only the controller is started: no templates are needed (the pages are TFAN26-47/48)
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ModuleController(moduleService))
                .build();
    }

    // ===== GET /modules/add =====

    @Test
    void shouldShowAddForm() throws Exception {
        mockMvc.perform(get("/modules/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"));
    }

    // ===== POST /modules/add : success =====

    @Test
    void shouldAddModuleAndRedirectWithSuccessMessage() throws Exception {
        when(moduleService.createModule("MOIA")).thenReturn(new ModuleEntity("MOIA"));

        mockMvc.perform(post("/modules/add").param("name", "MOIA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/modules"))
                .andExpect(flash().attribute("successKey", "success.module.created"))
                .andExpect(flash().attribute("createdModuleName", "MOIA"));

        verify(moduleService).createModule("MOIA");
    }

        // ===== POST /modules/add : errors =====

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


        // ===== GET /modules =====

    @Test
    void shouldListModules() throws Exception {
        List<ModuleEntity> modules = List.of(new ModuleEntity("MOIA"), new ModuleEntity("TF"));
        when(moduleService.findAll()).thenReturn(modules);

        mockMvc.perform(get("/modules"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"))
                .andExpect(model().attribute("modules", modules));
    }
}