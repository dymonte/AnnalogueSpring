package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.EmailAlreadyExistsException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private HandleUserService handleUserService;

    // ===== GET /members =====

    @Test
    @WithMockUser
    void membersShouldReturnAddMemberView() throws Exception {
        when(roleService.getRoles())
                .thenReturn(Arrays.asList(Role.values()));

        mockMvc.perform(
                get("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute("roles", Arrays.asList(Role.values())));
    }

    @Test
    @WithMockUser
    void formShouldContainEmailFieldAndAllRoles() throws Exception {
        when(roleService.getRoles())
                .thenReturn(Arrays.asList(Role.values()));

        mockMvc.perform(
                get("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"email\"")))
                .andExpect(content().string(containsString("value=\"ADMIN\"")))
                .andExpect(content().string(containsString("value=\"PROF\"")))
                .andExpect(content().string(containsString("value=\"ETUDIANT\"")));
    }

    // ===== POST /members =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectWithSuccessMessageWhenMemberIsAdded() throws Exception {
        when(handleUserService.addUser(
                "new@test.com",
                Role.ETUDIANT,
                2026,
                "G1"
        )).thenReturn("tmp12345");

        mockMvc.perform(
                post("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "ETUDIANT")
                        .param("annee", "2026")
                        .param("groupe", "G1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute(
                        "successKey",
                        "success.member.added"
                ))
                .andExpect(flash().attribute(
                        "addedEmail",
                        "new@test.com"
                ));

        verify(handleUserService).addUser(
                "new@test.com",
                Role.ETUDIANT,
                2026,
                "G1"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDisplaySuccessAlertAfterRedirect() throws Exception {
        when(roleService.getRoles())
                .thenReturn(Arrays.asList(Role.values()));

        mockMvc.perform(
                get("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .flashAttr("successKey", "success.member.added")
                        .flashAttr("addedEmail", "new@test.com")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alert-success")))
                .andExpect(content().string(containsString("new@test.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowEmailErrorWhenEmailAlreadyExists() throws Exception {
        when(roleService.getRoles())
                .thenReturn(Arrays.asList(Role.values()));

        when(handleUserService.addUser(
                "used@test.com",
                Role.ETUDIANT,
                2026,
                "G1"
        )).thenThrow(
                new EmailAlreadyExistsException(
                        "Account with this email already exists"
                )
        );

        mockMvc.perform(
                post("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "used@test.com")
                        .param("role", "ETUDIANT")
                        .param("annee", "2026")
                        .param("groupe", "G1")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute(
                        "emailErrorKey",
                        "error.member.email.exists"
                ))
                .andExpect(model().attribute(
                        "email",
                        "used@test.com"
                ))
                .andExpect(model().attribute(
                        "annee",
                        2026
                ))
                .andExpect(model().attribute(
                        "groupe",
                        "G1"
                ))
                .andExpect(content().string(
                        containsString("is-invalid")
                ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowGeneralErrorWhenSomethingElseFails() throws Exception {
        when(roleService.getRoles())
                .thenReturn(Arrays.asList(Role.values()));

        when(handleUserService.addUser(
                "new@test.com",
                Role.PROF,
                2026,
                "G1"
        )).thenThrow(
                new RuntimeException("Database down")
        );

        mockMvc.perform(
                post("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "PROF")
                        .param("annee", "2026")
                        .param("groupe", "G1")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute(
                        "errorKey",
                        "error.server"
                ))
                .andExpect(content().string(
                        containsString("alert-danger")
                ));
    }
}