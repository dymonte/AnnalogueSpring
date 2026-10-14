package com.annalogue.m2.ldap;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.services.HandleUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // TFAN26-12: fake service, so no real user is created during the tests
    @MockitoBean
    private HandleUserService handleUserService;

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
                // « My modules » is the home page of the non-admin users
                .andExpect(view().name("modules/list"));
    }

    // ===== TFAN26-12: only an admin can create members =====

    @Test
    void addMemberPageShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/members/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void studentShouldNotAccessAddMemberPage() throws Exception {

        mockMvc.perform(
                        get("/members/add")
                                .with(user("student@test.com").roles("ETUDIANT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void professorShouldNotAccessAddMemberPage() throws Exception {

        mockMvc.perform(
                        get("/members/add")
                                .with(user("prof@test.com").roles("PROF"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void studentShouldNotBeAbleToCreateMember() throws Exception {

        mockMvc.perform(
                        post("/members/add")
                                .with(user("student@test.com").roles("ETUDIANT"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("email", "new@test.com")
                                .param("role", "ADMIN")
                                .param("enteredUsername", "new")
                                .param("year", "2026").param("level", "M1")
                                .param("group", "A")
                )
                .andExpect(status().isForbidden());

        // the service must never be called
        verify(handleUserService, never()).addUser(any(), any(), any(),any(), any(), any());
    }

    @Test
    void adminShouldAccessAddMemberPage() throws Exception {

        mockMvc.perform(
                        get("/members/add")
                                .with(user("admin@test.com").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"));
    }

    @Test
    void adminShouldBeAbleToCreateMember() throws Exception {

        mockMvc.perform(
                        post("/members/add")
                                .with(user("admin@test.com").roles("ADMIN"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("email", "new@test.com")
                                .param("role", "ETUDIANT")
                                .param("enteredUsername", "new")
                                .param("year", "2026").param("level", "M1")
                                .param("group", "A")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/add"));

        verify(handleUserService).addUser("new@test.com", Role.ETUDIANT, "new", 2026, "M1", "A");
    }

    @Test
    void authenticatedUserShouldBeAbleToLogout() throws Exception {

        mockMvc.perform(
                        post("/logout")
                                .with(user("test@test.com"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }


    @Test
    void logoutShouldInvalidateSession() throws Exception {

        MockHttpSession session = new MockHttpSession();

        // Given : User is authenticated
        mockMvc.perform(
                        get("/home")
                                .session(session)
                                .with(user("test@test.com"))
                )
                .andExpect(status().isOk());

        // Logout
        mockMvc.perform(
                        post("/logout")
                                .session(session)
                                .with(user("test@test.com"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        // Session must be not usefully like authenticated session anymore
        mockMvc.perform(
                        get("/home")
                                .session(session)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void logoutShouldRequireCsrf() throws Exception {

        mockMvc.perform(
                        post("/logout")
                                .with(user("test@test.com"))
                )
                .andExpect(status().isForbidden());
    }

    // ===== Delete a member: admin only =====

    @Test
    void membersListShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void studentShouldNotBeAbleToDeleteMember() throws Exception {

        mockMvc.perform(
                        post("/members/1/delete")
                                .with(user("student@example.com").roles("ETUDIANT"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(status().isForbidden());

        verify(handleUserService, never()).deleteUser(any());
    }

    @Test
    void professorShouldNotBeAbleToDeleteMember() throws Exception {

        mockMvc.perform(
                        post("/members/1/delete")
                                .with(user("prof@example.com").roles("PROF"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(status().isForbidden());

        verify(handleUserService, never()).deleteUser(any());
    }

    @Test
    void adminShouldBeAbleToDeleteMember() throws Exception {

        mockMvc.perform(
                        post("/members/1/delete")
                                .with(user("admin@example.com").roles("ADMIN"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"));

        verify(handleUserService).deleteUser(1L);
    }

    // ===== TFAN26-117: module pages require authentication =====

    @Test
    void addModulePageShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/modules/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void modulesShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/modules"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ===== TFAN26-125 (ADMIN-02): only the admin changes a role =====

    @Test
    void nonAdminShouldNotChangeRoles() throws Exception {

        mockMvc.perform(
                        post("/members/1/role")
                                .with(user("prof@example.com").roles("RESPONSABLE"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("role", "RESPONSABLE")
                )
                .andExpect(status().isForbidden());

        verify(handleUserService, never()).changeRole(any(), any());
    }

    // ===== only a Responsable Module creates modules =====

    @Test
    void professorShouldNotAccessAddModulePage() throws Exception {

        mockMvc.perform(
                        get("/modules/add")
                                .with(user("prof@test.com").roles("PROF"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void studentShouldNotAccessAddModulePage() throws Exception {

        mockMvc.perform(
                        get("/modules/add")
                                .with(user("student@test.com").roles("ETUDIANT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldNotCreateModule() throws Exception {

        mockMvc.perform(
                        post("/modules/add")
                                .with(user("admin@test.com").roles("ADMIN"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("name", "MOIA")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void responsableShouldAccessAddModulePage() throws Exception {

        mockMvc.perform(
                        get("/modules/add")
                                .with(user("resp@test.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"));
    }
}