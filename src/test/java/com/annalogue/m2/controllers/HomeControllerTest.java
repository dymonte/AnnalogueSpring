package com.annalogue.m2.controllers;

import com.annalogue.m2.dto.UserModuleView;
import com.annalogue.m2.ldap.SecurityConfig;
import com.annalogue.m2.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

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
    private IUserService userService;

    @MockitoBean
    private IModuleService moduleService;

    @MockitoBean
    private INotificationService notificationService;

    @MockitoBean
    private HandPasswordService handPasswordService;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private NotifyService notifyService;

    @MockitoBean
    private HandleUserService handleUserService;

    // modules of Fabrice: MOIA created by him, TF where he is lecturer and editor
    private final UserModuleView moia =
            new UserModuleView(1L, "MOIA", UserModuleView.RESPONSIBLE_ROLE_KEY, false);
    private final UserModuleView tf =
            new UserModuleView(2L, "TF", "my.modules.role.INTERVENANT", true);

    @Test
    void homeAuthenticatedShouldReturnHomePage() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("user1").roles("USER"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"));
    }

    @Test
    void homeUnauthenticatedShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));
    }

    @Test
    void userAuthenticatedAsStudentShouldNotShowTheButton() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("user1").roles("ETUDIANT"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"))
                .andExpect(model().attribute("canCreateModuleAndPublication", false));
    }

    @Test
    void userAuthenticatedAsProfessorShouldShowTheButton() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("prof1").roles("PROF"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"))
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

    // ===== TFAN26-67 (UTIL-04): « My modules » is the home page =====

    @Test
    void homeShowsAllTheModulesOfTheUser() throws Exception {
        when(moduleService.findModulesOfUser("fabrice@example.com")).thenReturn(List.of(moia, tf));

        mockMvc.perform(
                        get("/home")
                                .with(user("fabrice@example.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"))
                .andExpect(model().attribute("modules", List.of(moia, tf)))
                .andExpect(model().attribute("filter", "all"))
                .andExpect(model().attribute("countAll", 2))
                .andExpect(model().attribute("countCreated", 1))
                .andExpect(model().attribute("countEnrolled", 1))
                .andExpect(model().attribute("isAdmin", false))
                .andExpect(model().attribute("login", "fabrice@example.com"));
    }

    @Test
    void homeShowsOnlyTheModulesCreatedByTheUser() throws Exception {
        when(moduleService.findModulesOfUser("fabrice@example.com")).thenReturn(List.of(moia, tf));

        mockMvc.perform(
                        get("/home").param("filter", "created")
                                .with(user("fabrice@example.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("modules", List.of(moia)))
                .andExpect(model().attribute("filter", "created"))
                .andExpect(model().attribute("countAll", 2));
    }

    @Test
    void homeShowsOnlyTheModulesTheUserIsEnrolledIn() throws Exception {
        when(moduleService.findModulesOfUser("fabrice@example.com")).thenReturn(List.of(moia, tf));

        mockMvc.perform(
                        get("/home").param("filter", "enrolled")
                                .with(user("fabrice@example.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("modules", List.of(tf)))
                .andExpect(model().attribute("filter", "enrolled"));
    }

    @Test
    void unknownFilterShowsAllTheModules() throws Exception {
        when(moduleService.findModulesOfUser("fabrice@example.com")).thenReturn(List.of(moia, tf));

        mockMvc.perform(
                        get("/home").param("filter", "fantome")
                                .with(user("fabrice@example.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("modules", List.of(moia, tf)))
                .andExpect(model().attribute("filter", "all"));
    }

    @Test
    void studentWithoutModuleGetsAnEmptyList() throws Exception {
        when(moduleService.findModulesOfUser("fabien@example.com")).thenReturn(List.of());

        mockMvc.perform(
                        get("/home")
                                .with(user("fabien@example.com").roles("ETUDIANT"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"))
                .andExpect(model().attribute("modules", List.of()))
                .andExpect(model().attribute("countAll", 0));
    }

    @Test
    void adminHomeDoesNotLoadModules() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("admin@example.com").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeDoesNotExist("modules"));

        verify(moduleService, never()).findModulesOfUser(any());
    }

        // ===== TFAN26-66 (UTIL-04): « My modules » page =====

    @Test
    void pageShowsTheModulesWithTheirLabels() throws Exception {
        when(moduleService.findModulesOfUser("fabrice@example.com")).thenReturn(List.of(moia, tf));

        mockMvc.perform(
                        get("/home")
                                .with(user("fabrice@example.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My modules")))
                .andExpect(content().string(containsString("MOIA")))
                .andExpect(content().string(containsString("href=\"/modules/1\"")))
                .andExpect(content().string(containsString("Created by me")))
                .andExpect(content().string(containsString("Lecturer")))
                .andExpect(content().string(containsString("Editor")));
    }

    @Test
    void responsableSeesTheCreateButtonAndTheCreatedFilter() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("resp@example.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"create-module\"")))
                .andExpect(content().string(containsString("id=\"filter-created\"")));
    }

    @Test
    void professorDoesNotSeeTheCreateButton() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("prof@example.com").roles("PROF"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("id=\"create-module\""))));
    }

    @Test
    void studentWithoutModuleSeesTheEmptyMessage() throws Exception {
        mockMvc.perform(
                        get("/home")
                                .with(user("fabien@example.com").roles("ETUDIANT"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You are not in any module yet.")))
                .andExpect(content().string(containsString("id=\"filter-enrolled\"")))
                .andExpect(content().string(not(containsString("id=\"filter-created\""))));
    }

    @Test
    void emptyCreatedFilterShowsItsMessage() throws Exception {
        when(moduleService.findModulesOfUser("resp@example.com")).thenReturn(List.of(tf));

        mockMvc.perform(
                        get("/home").param("filter", "created")
                                .with(user("resp@example.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have not created any module yet.")));
    }
}