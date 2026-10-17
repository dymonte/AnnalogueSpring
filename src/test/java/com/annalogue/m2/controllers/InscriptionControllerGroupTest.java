package com.annalogue.m2.controllers;

import com.annalogue.m2.dto.GroupEnrollmentResult;
import com.annalogue.m2.exceptions.ModuleNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotManagedException;
import com.annalogue.m2.ldap.SecurityConfig;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// TFAN26-106: endpoint to register a whole group in a module
@WebMvcTest(InscriptionController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class InscriptionControllerGroupTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private IInscriptionService inscriptionService;

    @MockitoBean
    private INotifyService notifyService;

    @MockitoBean 
    private INotificationService notificationService;

    private UserEntity responsible;

    @BeforeEach
    void setUp() {
        responsible = new UserEntity("resp@ufr.com", "hash", "resp", Role.RESPONSABLE, null, null);
        responsible.setId(1L);

        when(userService.findByEmail("resp@ufr.com")).thenReturn(Optional.of(responsible));
    }

    @Test
    void responsibleShouldRegisterAGroup() throws Exception {
        when(inscriptionService.saveGroup(responsible, "G1", 100L))
                .thenReturn(new GroupEnrollmentResult(2, 1));
        when(notifyService.getMessage("success.inscription.group.created", 2, 1))
                .thenReturn("Group registered");

        mockMvc.perform(post("/module-enrollments/group")
                .with(user("resp@ufr.com").roles("RESPONSABLE"))
                .with(csrf())
                .param("moduleId", "100")
                .param("group", "G1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attribute("success", "Group registered"));

        verify(inscriptionService).saveGroup(responsible, "G1", 100L);
    }

    @Test
    void shouldShowErrorWhenModuleIsNotManaged() throws Exception {
        when(inscriptionService.saveGroup(any(), any(), any()))
                .thenThrow(new ModuleNotManagedException("error.inscription.module.not.managed"));
        when(notifyService.getMessage("error.inscription.module.not.managed"))
                .thenReturn("Not allowed");

        mockMvc.perform(post("/module-enrollments/group")
                .with(user("resp@ufr.com").roles("RESPONSABLE"))
                .with(csrf())
                .param("moduleId", "100")
                .param("group", "G1"))
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attribute("error", "Not allowed"));
    }

    @Test
    void shouldShowErrorWhenModuleDoesNotExist() throws Exception {
        when(inscriptionService.saveGroup(any(), any(), any()))
                .thenThrow(new ModuleNotFoundException("error.inscription.module.not.found"));
        when(notifyService.getMessage("error.inscription.module.not.found"))
                .thenReturn("Module not found");

        mockMvc.perform(post("/module-enrollments/group")
                .with(user("resp@ufr.com").roles("RESPONSABLE"))
                .with(csrf())
                .param("moduleId", "999")
                .param("group", "G1"))
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attribute("error", "Module not found"));
    }

    @Test
    void shouldShowErrorWhenGroupHasNoMember() throws Exception {
        when(inscriptionService.saveGroup(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("error.inscription.group.not.found"));
        when(notifyService.getMessage("error.inscription.group.not.found"))
                .thenReturn("No member");

        mockMvc.perform(post("/module-enrollments/group")
                .with(user("resp@ufr.com").roles("RESPONSABLE"))
                .with(csrf())
                .param("moduleId", "100")
                .param("group", "Z"))
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attribute("error", "No member"));
    }

    @Test
    void shouldShowErrorWhenGroupIsMissing() throws Exception {
        when(inscriptionService.saveGroup(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("error.inscription.group.required"));
        when(notifyService.getMessage("error.inscription.group.required"))
                .thenReturn("Select a group");

        mockMvc.perform(post("/module-enrollments/group")
                .with(user("resp@ufr.com").roles("RESPONSABLE"))
                .with(csrf())
                .param("moduleId", "100"))
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attribute("error", "Select a group"));
    }

    @Test
    void shouldShowGeneralErrorOnUnexpectedFailure() throws Exception {
        when(inscriptionService.saveGroup(any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));
        when(notifyService.getMessage("error.server")).thenReturn("Server error");

        mockMvc.perform(post("/module-enrollments/group")
                .with(user("resp@ufr.com").roles("RESPONSABLE"))
                .with(csrf())
                .param("moduleId", "100")
                .param("group", "G1"))
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attribute("error", "Server error"));
    }

    @Test
    void studentShouldNotRegisterAGroup() throws Exception {
        mockMvc.perform(post("/module-enrollments/group")
                .with(user("student@ufr.com").roles("ETUDIANT"))
                .with(csrf())
                .param("moduleId", "100")
                .param("group", "G1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inscriptionService);
    }

    @Test
    void adminShouldNotRegisterAGroup() throws Exception {
        mockMvc.perform(post("/module-enrollments/group")
                .with(user("admin@ufr.com").roles("ADMIN"))
                .with(csrf())
                .param("moduleId", "100")
                .param("group", "G1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inscriptionService);
    }

    @Test
    void anonymousShouldBeRedirectedToLogin() throws Exception {
        mockMvc.perform(post("/module-enrollments/group")
                .with(csrf())
                .param("moduleId", "100")
                .param("group", "G1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("/login")));

        verifyNoInteractions(inscriptionService);
    }
}