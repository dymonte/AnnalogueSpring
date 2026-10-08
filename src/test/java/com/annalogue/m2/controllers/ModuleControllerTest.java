package com.annalogue.m2.controllers;

import com.annalogue.m2.services.IModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}