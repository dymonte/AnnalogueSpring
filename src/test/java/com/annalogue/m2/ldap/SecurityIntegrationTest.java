package com.annalogue.m2.ldap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginShouldBeAccessibleWithoutAuthentication() throws Exception {

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void homeShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void homeShouldBeAccessibleForAuthenticatedUser() throws Exception {

        mockMvc.perform(
                        get("/home")
                                .with(user("test@test.com"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

}