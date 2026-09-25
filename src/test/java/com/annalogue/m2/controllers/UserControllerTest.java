package com.annalogue.m2.controllers;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private HandleUserService handleUserService;

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
}