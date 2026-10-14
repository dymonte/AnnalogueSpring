package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.EmailAlreadyExistsException;
import com.annalogue.m2.exceptions.InvalidUserDataException;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;
import com.annalogue.m2.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.annalogue.m2.services.HandPasswordService;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;
import static org.hamcrest.Matchers.not;

import com.annalogue.m2.services.NotifyService;
import org.springframework.context.annotation.Import;
import com.annalogue.m2.exceptions.UserDeletionException;
import com.annalogue.m2.models.UserEntity;
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(NotifyService.class)
class UserControllerTest {
        private static final List<Role> ASSIGNABLE_ROLES = List.of(Role.PROF, Role.ETUDIANT);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private HandleUserService handleUserService;

    @MockitoBean
    private HandPasswordService handPasswordService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;
    // tells the RoleService mock that this role exists
    private void mockValidRole(String roleName, Role role) {
        when(roleService.exists(roleName)).thenReturn(true);
        when(roleService.fromString(roleName)).thenReturn(role);
        when(roleService.getAssignableRoles()).thenReturn(ASSIGNABLE_ROLES);
    }

    // ===== GET /members/add =====

    @Test
    @WithMockUser
    void addMemberPageShouldReturnAddMemberView() throws Exception {
        when(roleService.getAssignableRoles())
                .thenReturn(ASSIGNABLE_ROLES);

        mockMvc.perform(
                get("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute("roles", ASSIGNABLE_ROLES))
                .andExpect(model().attributeExists("academicYears"))
                .andExpect(model().attributeExists("levels"));
    }

    @Test
    @WithMockUser
    void formShouldContainAllFieldsRolesAndGroups() throws Exception {
        when(roleService.getAssignableRoles())
                .thenReturn(ASSIGNABLE_ROLES);

        mockMvc.perform(
                get("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"email\"")))
                .andExpect(content().string(containsString("name=\"enteredUsername\"")))
                .andExpect(content().string(containsString("name=\"year\"")))
                .andExpect(content().string(containsString("name=\"group\"")))
                .andExpect(content().string(not(containsString("value=\"ADMIN\""))))
                .andExpect(content().string(containsString("value=\"PROF\"")))
                .andExpect(content().string(containsString("value=\"ETUDIANT\"")))
                .andExpect(content().string(containsString("value=\"A\"")))
                .andExpect(content().string(containsString("value=\"D\"")))
                .andExpect(content().string(containsString("name=\"level\"")))
                .andExpect(content().string(containsString("value=\"M1\"")));
    }

    // ===== POST /members/add : success =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectWithSuccessMessageWhenMemberIsAdded() throws Exception {
        mockValidRole("ETUDIANT", Role.ETUDIANT);
        when(handleUserService.addUser("new@test.com", Role.ETUDIANT, "new", 2026, "M1", "A"))
                .thenReturn("tmp12345");

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "ETUDIANT")
                        .param("enteredUsername", "new")
                        .param("year", "2026").param("level", "M1")
                        .param("group", "A")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/add"))
                .andExpect(flash().attribute("successKey", "success.member.added"))
                .andExpect(flash().attribute("addedEmail", "new@test.com"));

        verify(handleUserService).addUser("new@test.com", Role.ETUDIANT, "new", 2026, "M1", "A");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDisplaySuccessAlertAfterRedirect() throws Exception {
        when(roleService.getAssignableRoles())
                .thenReturn(ASSIGNABLE_ROLES);

        mockMvc.perform(
                get("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .flashAttr("successKey", "success.member.added")
                        .flashAttr("addedEmail", "new@test.com")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alert-success")))
                .andExpect(content().string(containsString("new@test.com")));
    }

    // ===== POST /members/add : specific errors =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowEmailErrorWhenEmailAlreadyExists() throws Exception {
        mockValidRole("ETUDIANT", Role.ETUDIANT);
        when(handleUserService.addUser("used@test.com", Role.ETUDIANT, "used", 2026, "M1", "A"))
                .thenThrow(new EmailAlreadyExistsException("Account with this email already exists"));

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "used@test.com")
                        .param("role", "ETUDIANT")
                        .param("enteredUsername", "used")
                        .param("year", "2026").param("level", "M1")
                        .param("group", "A")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute("emailErrorKey", "error.member.email.exists"))
                .andExpect(model().attribute("email", "used@test.com"))
                .andExpect(model().attribute("year", 2026))
                .andExpect(model().attribute("group", "A"))
                .andExpect(content().string(containsString("is-invalid")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowUsernameErrorWhenUsernameIsInvalid() throws Exception {
        mockValidRole("ETUDIANT", Role.ETUDIANT);
        when(handleUserService.addUser("new@test.com", Role.ETUDIANT, "a b!", 2026, "M1", "A"))
                .thenThrow(new InvalidUserDataException("username", "error.user.username.invalid"));

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "ETUDIANT")
                        .param("enteredUsername", "a b!")
                        .param("year", "2026").param("level", "M1")
                        .param("group", "A")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute("usernameErrorKey", "error.user.username.invalid"))
                .andExpect(content().string(containsString("is-invalid")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowGroupErrorWhenGroupIsInvalid() throws Exception {
        mockValidRole("ETUDIANT", Role.ETUDIANT);
        when(handleUserService.addUser("new@test.com", Role.ETUDIANT, "new", 2026, "M1", "Z"))
                .thenThrow(new InvalidUserDataException("group", "error.user.group.invalid"));

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "ETUDIANT")
                        .param("enteredUsername", "new")
                        .param("year", "2026")
                        .param("level", "M1")
                        .param("group", "Z")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute("groupErrorKey", "error.user.group.invalid"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowRoleErrorWhenRoleIsEmpty() throws Exception {
        when(roleService.getAssignableRoles())
                .thenReturn(ASSIGNABLE_ROLES);

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "")
                        .param("enteredUsername", "new")
                        .param("year", "2026").param("level", "M1").param("level", "M1")
                        .param("group", "A")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("roleErrorKey", "error.user.role.empty"));

        verify(handleUserService, never()).addUser(any(), any(),any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowRoleErrorWhenRoleIsUnknown() throws Exception {
        when(roleService.exists("DIRECTEUR")).thenReturn(false);
        when(roleService.getAssignableRoles())
                .thenReturn(ASSIGNABLE_ROLES);

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "DIRECTEUR")
                        .param("enteredUsername", "new")
                        .param("year", "2026").param("level", "M1")
                        .param("group", "A")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("roleErrorKey", "error.user.role.invalid"));

        verify(handleUserService, never()).addUser(any(), any(),any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectYearThatIsNotANumber() throws Exception {
        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "ETUDIANT")
                        .param("enteredUsername", "new")
                        .param("year", "abc")
                        .param("group", "A")
        )
                .andExpect(status().isBadRequest());

        verify(handleUserService, never()).addUser(any(), any(),any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowDatabaseErrorWhenSavingFails() throws Exception {
        mockValidRole("PROF", Role.PROF);
        when(handleUserService.addUser("new@test.com", Role.PROF, "new", 2026, "M1", "A"))
                .thenThrow(new DataAccessResourceFailureException("Database down"));

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "PROF")
                        .param("enteredUsername", "new")
                        .param("year", "2026").param("level", "M1")
                        .param("group", "A")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute("errorKey", "error.database"))
                .andExpect(content().string(containsString("alert-danger")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowErrorWhenTryingToCreateAnAdmin() throws Exception {
        mockValidRole("ADMIN", Role.ADMIN);
        when(handleUserService.addUser("admin2@test.com", Role.ADMIN, "admin2", 2026, "M1", "A"))
                .thenThrow(new InvalidUserDataException("role", "error.user.role.admin.forbidden"));

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "admin2@test.com")
                        .param("role", "ADMIN")
                        .param("enteredUsername", "admin2")
                        .param("year", "2026").param("level", "M1")
                        .param("group", "A")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_member"))
                .andExpect(model().attribute("roleErrorKey", "error.user.role.admin.forbidden"))
                .andExpect(content().string(containsString("is-invalid")));
    }


        @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowLevelErrorWhenLevelIsUnknown() throws Exception {
        mockValidRole("ETUDIANT", Role.ETUDIANT);
        when(handleUserService.addUser("new@test.com", Role.ETUDIANT, "new", 2026, "M3", "A"))
                .thenThrow(new InvalidUserDataException("level", "error.user.level.invalid"));

        mockMvc.perform(
                post("/members/add")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "new@test.com")
                        .param("role", "ETUDIANT")
                        .param("enteredUsername", "new")
                        .param("year", "2026").param("level", "M3")
                        .param("group", "A")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("levelErrorKey", "error.user.level.invalid"))
                .andExpect(model().attribute("selectedLevel", "M3"));
    }

    // ===== GET /members/username-check : instant check =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void usernameCheckShouldSayAvailable() throws Exception {
        when(handleUserService.isUsernameAvailable("jkamel")).thenReturn(true);

        mockMvc.perform(get("/members/enteredUsername-check").param("enteredUsername", "jkamel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.suggestion").value("jkamel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void usernameCheckShouldSuggestAnotherNameWhenTaken() throws Exception {
        when(handleUserService.isUsernameAvailable("jkamel")).thenReturn(false);
        when(handleUserService.suggestUsername("jkamel")).thenReturn("jkamel1");

        mockMvc.perform(get("/members/enteredUsername-check").param("enteredUsername", "jkamel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.suggestion").value("jkamel1"));
    }

        // ===== GET /members =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListMembers() throws Exception {
        UserEntity student = new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, null, null);
        when(handleUserService.findAllUsers()).thenReturn(List.of(student));

        mockMvc.perform(get("/members").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/members"))
                .andExpect(content().string(containsString("data-email=\"memb@example.com\"")))
                .andExpect(content().string(containsString("Student")));
    }

    // ===== POST /members/{id}/delete =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteMemberAndRedirectWithSuccessMessage() throws Exception {
        when(handleUserService.deleteUser(5L)).thenReturn("memb");

        mockMvc.perform(post("/members/5/delete").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("successKey", "success.member.deleted"))
                .andExpect(flash().attribute("deletedUsername", "memb"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectWithErrorWhenUserCannotBeDeleted() throws Exception {
        when(handleUserService.deleteUser(6L))
                .thenThrow(new UserDeletionException("error.user.delete.linked"));

        mockMvc.perform(post("/members/6/delete").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("errorKey", "error.user.delete.linked"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowDeleteSuccessMessageAfterRedirect() throws Exception {
        when(handleUserService.findAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .flashAttr("successKey", "success.member.deleted")
                        .flashAttr("deletedUsername", "memb"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User memb was deleted.")));
    }

    // ===== POST /members/{id}/role : Prof <-> Responsable Module =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldChangeRoleAndRedirectWithSuccessMessage() throws Exception {
        mockValidRole("RESPONSABLE", Role.RESPONSABLE);
        when(handleUserService.changeRole(7L, Role.RESPONSABLE)).thenReturn("toto");

        mockMvc.perform(post("/members/7/role")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("role", "RESPONSABLE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("successKey", "success.member.role.changed"))
                .andExpect(flash().attribute("changedUsername", "toto"));

        verify(handleUserService).changeRole(7L, Role.RESPONSABLE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRefuseEmptyRoleWhenChangingRole() throws Exception {
        mockMvc.perform(post("/members/7/role")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("role", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("errorKey", "error.user.role.empty"));

        verify(handleUserService, never()).changeRole(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRefuseUnknownRoleWhenChangingRole() throws Exception {
        when(roleService.exists("FANTOME")).thenReturn(false);

        mockMvc.perform(post("/members/7/role")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("role", "FANTOME"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorKey", "error.user.role.invalid"));

        verify(handleUserService, never()).changeRole(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowErrorWhenRoleChangeIsRefused() throws Exception {
        mockValidRole("ADMIN", Role.ADMIN);
        when(handleUserService.changeRole(7L, Role.ADMIN))
                .thenThrow(new InvalidUserDataException("role", "error.user.role.change.admin"));

        mockMvc.perform(post("/members/7/role")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("errorKey", "error.user.role.change.admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowDatabaseErrorWhenRoleCannotBeSaved() throws Exception {
        mockValidRole("PROF", Role.PROF);
        when(handleUserService.changeRole(7L, Role.PROF))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        mockMvc.perform(post("/members/7/role")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("role", "PROF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorKey", "error.user.role.change.database"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowRoleSelectOnlyForTeachers() throws Exception {
        UserEntity toto = new UserEntity("toto@example.com", "hash", "toto", Role.PROF, null, null);
        toto.setId(7L);
        UserEntity student = new UserEntity("memb@example.com", "hash", "memb", Role.ETUDIANT, null, null);
        student.setId(8L);
        when(handleUserService.findAllUsers()).thenReturn(List.of(toto, student));
        when(roleService.getTeacherRoles()).thenReturn(List.of(Role.PROF, Role.RESPONSABLE));

        mockMvc.perform(get("/members").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/members/7/role")))
                .andExpect(content().string(not(containsString("/members/8/role"))))
                .andExpect(content().string(containsString("Module manager")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowRoleChangedMessageAfterRedirect() throws Exception {
        when(handleUserService.findAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/members")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .flashAttr("successKey", "success.member.role.changed")
                        .flashAttr("changedUsername", "toto"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The role of toto was changed.")));
    }

    // ===== GET /members?role=... : filter by role =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFilterMembersByRole() throws Exception {
        UserEntity toto = new UserEntity("toto@example.com", "hash", "toto", Role.PROF, null, null);
        toto.setId(7L);
        mockValidRole("PROF", Role.PROF);
        when(handleUserService.findUsersByRole(Role.PROF)).thenReturn(List.of(toto));

        mockMvc.perform(get("/members").param("role", "PROF").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedFilter", "PROF"))
                .andExpect(content().string(containsString("data-email=\"toto@example.com\"")));

        verify(handleUserService, never()).findAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAllMembersWhenFilterIsUnknown() throws Exception {
        when(roleService.exists("FANTOME")).thenReturn(false);
        when(handleUserService.findAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/members").param("role", "FANTOME").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedFilter", ""))
                .andExpect(content().string(containsString("No members yet.")));

        verify(handleUserService, never()).findUsersByRole(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSayNoMemberWithThisRole() throws Exception {
        mockValidRole("RESPONSABLE", Role.RESPONSABLE);
        when(handleUserService.findUsersByRole(Role.RESPONSABLE)).thenReturn(List.of());

        mockMvc.perform(get("/members").param("role", "RESPONSABLE").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No members with this role.")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldKeepTheFilterAfterChangingRole() throws Exception {
        mockValidRole("RESPONSABLE", Role.RESPONSABLE);
        when(handleUserService.changeRole(7L, Role.RESPONSABLE)).thenReturn("toto");

        mockMvc.perform(post("/members/7/role")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("role", "RESPONSABLE")
                        .param("filter", "PROF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members?role=PROF"));
    }
}