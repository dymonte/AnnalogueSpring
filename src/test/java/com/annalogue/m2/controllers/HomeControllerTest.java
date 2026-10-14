package com.annalogue.m2.controllers;

import com.annalogue.m2.ldap.SecurityConfig;
import com.annalogue.m2.services.CustomUserDetailsService;
import com.annalogue.m2.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserService userService;

    @Test
    void homeAuthenticatedShouldReturnHomePage() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("user1").roles("USER"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    void homeUnauthenticatedShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void userAuthenticatedAsStudentShouldNotShowTheButton() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("user1").roles("USER"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("canCreateModuleAndPublication", false));
    }

    @Test
    void userAuthenticatedAsProfessorShouldShowTheButton() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("prof1").roles("PROF"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("canCreateModuleAndPublication", true));
    }

    @Test
    void userAuthenticatedAsAdminShouldShowTheButton() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("prof1").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("canCreateModuleAndPublication", true));
    }

    @Test
    void responsableShouldSeeCreateModule() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("resp1").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("canCreateModuleAndPublication", true))
                .andExpect(model().attribute("canCreateModule", true));
    }

    @Test
    void professorShouldNotSeeCreateModule() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("prof1").roles("PROF"))
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("canCreateModule", false));
    }

    @Test
    void adminShouldNotSeeCreateModule() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("admin1").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("canCreateModule", false));
    }
}