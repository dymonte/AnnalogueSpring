package com.annalogue.m2.controllers;

import com.annalogue.m2.models.*;
import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModuleController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ModuleFormErrorsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IModuleService moduleService;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private INotificationService notificationService;

    @MockitoBean
    private IInscriptionService inscriptionService;

    private final UserEntity teacher =
            new UserEntity("fabrice@example.com", "hash", "fabrice", Role.PROF, null, null);

    @Test
    void emptyFormShouldShowNoError() throws Exception {
        mockMvc.perform(get("/modules/add").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("is-invalid"))))
                .andExpect(content().string(not(containsString("alert-danger"))));
    }

    @Test
    void shouldShowEmptyNameErrorUnderTheField() throws Exception {
        when(userService.getCurrentUser()).thenReturn(teacher);

        when(moduleService.createModule(
                eq("   "),
                eq(teacher)
        )).thenThrow(
                new InvalidModuleDataException("error.module.name.empty")
        );

        mockMvc.perform(post("/modules/add")
                        .param("name", "   ")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is-invalid")))
                .andExpect(content().string(containsString("invalid-feedback")))
                .andExpect(content().string(
                        containsString("The module name is required.")
                ));

        verify(userService).getCurrentUser();
        verify(moduleService).createModule("   ", teacher);
    }

    @Test
    void shouldShowTooLongNameError() throws Exception {
        String longName = "M".repeat(256);

        when(userService.getCurrentUser()).thenReturn(teacher);

        when(moduleService.createModule(
                eq(longName),
                eq(teacher)
        )).thenThrow(
                new InvalidModuleDataException("error.module.name.tooLong")
        );

        mockMvc.perform(post("/modules/add")
                        .param("name", longName)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(
                                "The module name must be at most 255 characters."
                        )
                ));

        verify(userService).getCurrentUser();
        verify(moduleService).createModule(longName, teacher);
    }

    @Test
    void shouldShowNameAlreadyUsedErrorAndKeepTheTypedName() throws Exception {
        UserEntity responsible = org.mockito.Mockito.mock(UserEntity.class);
        when(userService.getCurrentUser()).thenReturn(responsible);
        when(moduleService.createModule("MOIA", responsible))
                .thenThrow(new InvalidModuleDataException("error.module.name.exists"));

        mockMvc.perform(post("/modules/add")
                        .param("name", "MOIA")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is-invalid")))
                .andExpect(content().string(containsString(
                        "A module with this name already exists.")))
                .andExpect(content().string(containsString("value=\"MOIA\"")));
    }

    @Test
    void shouldShowDatabaseErrorAsGeneralAlert() throws Exception {
        UserEntity responsible = org.mockito.Mockito.mock(UserEntity.class);
        when(userService.getCurrentUser()).thenReturn(responsible);
        when(moduleService.createModule("MOIA", responsible))
                .thenThrow(new DataAccessResourceFailureException("Database down"));

        mockMvc.perform(post("/modules/add")
                        .param("name", "MOIA")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alert-danger")))
                .andExpect(content().string(containsString(
                        "The module could not be saved. Please try again later.")));
    }
}
