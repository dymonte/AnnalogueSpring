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


@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
public class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@MockitoBean
	private UserService userService;

	@Test
	void adminAuthenticatedAdminShouldReturnAdminPage() throws Exception {
		mockMvc.perform(
						get("/admin")
								.with(user("user1").roles("ADMIN"))
				)
				.andExpect(status().isOk())
				.andExpect(view().name("admin"));
	}

	@Test
	void adminUnauthenticatedShouldRedirectToLogin() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void adminAuthenticatedUserShouldBeForbidden() throws Exception {
		mockMvc.perform(
				get("/admin")
						.with(user("user1").roles("USER"))
				)
				.andExpect(status().isForbidden());
	}

	@Test
	void adminuthenticatedProfessorShouldBeForbidden() throws Exception {
		mockMvc.perform(
						get("/admin")
								.with(user("user1").roles("PROF"))
				)
				.andExpect(status().isForbidden());
	}
}