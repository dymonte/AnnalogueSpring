package com.annalogue.m2.controllers;

import com.annalogue.m2.services.NotifyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // For now
@ActiveProfiles("test")
@Import(NotifyService.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void loginShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // TFAN26-98: the login error comes from messages.properties
    @Test
    public void loginErrorShouldUseCentralizedMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Incorrect email or password."))
                .andExpect(content().string(containsString("Incorrect email or password.")));
    }

    @Test
    public void logoutMessageShouldUseCentralizedMessage() throws Exception {
        mockMvc.perform(get("/login").param("logout", "").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have been logged out.")));
    }
}