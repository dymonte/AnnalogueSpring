package com.annalogue.m2.controllers;

import com.annalogue.m2.dto.UserModuleView;
import com.annalogue.m2.exceptions.InvalidModuleDataException;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ModuleControllerTest {

    private final IModuleService moduleService = mock(IModuleService.class);
    private final IUserService userService = mock(IUserService.class);
    private final IInscriptionService inscriptionService = mock(IInscriptionService.class);

    private final UserEntity teacher =
            new UserEntity("fabrice@example.com", "hash", "fabrice", Role.PROF, null, null);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ModuleController(moduleService,userService, inscriptionService))
                .build();
    }

    @Test
    void shouldShowAddForm() throws Exception {
        mockMvc.perform(get("/modules/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"));
    }

    @Test
    void shouldAddModuleAndRedirectWithSuccessMessage() throws Exception {
        ModuleEntity createdModule = new ModuleEntity("MOIA", teacher);

        when(userService.getCurrentUser()).thenReturn(teacher);
        when(moduleService.createModule("MOIA", teacher))
                .thenReturn(createdModule);

        mockMvc.perform(post("/modules/add").param("name", "MOIA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute(
                        "successKey", "success.module.created"))
                .andExpect(flash().attribute(
                        "createdModuleName", "MOIA"));

        verify(userService).getCurrentUser();
        verify(moduleService).createModule("MOIA", teacher);
    }


    @Test
    void shouldShowErrorWhenNameIsEmpty() throws Exception {
        when(userService.getCurrentUser()).thenReturn(teacher);

        when(moduleService.createModule(
                eq(""),
                eq(teacher)
        )).thenThrow(
                new InvalidModuleDataException("error.module.name.empty")
        );

        mockMvc.perform(post("/modules/add").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute(
                        "nameErrorKey",
                        "error.module.name.empty"
                ));

        verify(userService).getCurrentUser();
        verify(moduleService).createModule("", teacher);
    }


    @Test
    void shouldShowErrorWhenNameIsMissing() throws Exception {
        when(userService.getCurrentUser()).thenReturn(teacher);

        when(moduleService.createModule(
                isNull(),
                eq(teacher)
        )).thenThrow(
                new InvalidModuleDataException("error.module.name.empty")
        );

        mockMvc.perform(post("/modules/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute(
                        "nameErrorKey",
                        "error.module.name.empty"
                ));

        verify(userService).getCurrentUser();
        verify(moduleService).createModule(null, teacher);
    }

    @Test
    void shouldShowErrorAndKeepNameWhenNameAlreadyExists() throws Exception {
        when(userService.getCurrentUser()).thenReturn(teacher);

        when(moduleService.createModule("MOIA",teacher))
                .thenThrow(new InvalidModuleDataException("error.module.name.exists"));

        mockMvc.perform(post("/modules/add").param("name", "MOIA"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("nameErrorKey", "error.module.name.exists"))
                .andExpect(model().attribute("name", "MOIA"));
        verify(userService).getCurrentUser();
        verify(moduleService).createModule("MOIA", teacher);
    }

    @Test
    void shouldShowErrorWhenNameIsTooLong() throws Exception {
        String longName = "M".repeat(256);
        when(userService.getCurrentUser()).thenReturn(teacher);

        when(moduleService.createModule(longName, teacher))
                .thenThrow(new InvalidModuleDataException("error.module.name.tooLong"));

        mockMvc.perform(post("/modules/add").param("name", longName))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("nameErrorKey", "error.module.name.tooLong"));
        verify(userService).getCurrentUser();
        verify(moduleService).createModule(longName, teacher);
    }

    @Test
    void shouldShowDatabaseErrorWhenSavingFails() throws Exception {
        when(userService.getCurrentUser()).thenReturn(teacher);

        when(moduleService.createModule("MOIA",teacher))
                .thenThrow(new DataAccessResourceFailureException("Database down"));

        mockMvc.perform(post("/modules/add").param("name", "MOIA"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add"))
                .andExpect(model().attribute("errorKey", "error.module.database"))
                .andExpect(model().attribute("name", "MOIA"));
        verify(userService).getCurrentUser();
        verify(moduleService).createModule("MOIA", teacher);
    }

    // ===== GET /modules: catalogue for the admin, home page for the others =====

    private UsernamePasswordAuthenticationToken loggedIn(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void adminSeesAllTheModules() throws Exception {
        List<UserModuleView> modules = List.of(
                new UserModuleView(1L, "MOIA", null, false),
                new UserModuleView(2L, "TF", null, false));
        when(moduleService.findAllModuleViews()).thenReturn(modules);

        mockMvc.perform(get("/modules").principal(loggedIn("admin@example.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/list"))
                .andExpect(model().attribute("modules", modules))
                .andExpect(model().attribute("countAll", 2))
                .andExpect(model().attribute("isAdmin", true));
    }

    @Test
    void teacherIsSentToHisHomePage() throws Exception {
        mockMvc.perform(get("/modules").principal(loggedIn("fabrice@example.com", "RESPONSABLE")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(moduleService, never()).findAllModuleViews();
    }

    @Test
    void studentIsSentToHisHomePage() throws Exception {
        mockMvc.perform(get("/modules").principal(loggedIn("fabien@example.com", "ETUDIANT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void shouldShowEmptyModulePage() throws Exception {
        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(1L);
        when(module.getName()).thenReturn("MOIA");
        when(module.getResponsible()).thenReturn(teacher);
        when(module.getPublications()).thenReturn(List.of());
        when(moduleService.findById(1L)).thenReturn(Optional.of(module));

        UserEntity user = new UserEntity("user@example.com", "hash", "user", Role.PROF, null, null);
        user.setId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(inscriptionService.hasReaderAccess(eq(user), any(ModuleEntity.class))).thenReturn(true);


        mockMvc.perform(get("/modules/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/detail"))
                .andExpect(model().attribute("publications", List.of()))
                .andExpect(model().attribute("module", module));
    }

    @Test
    void shouldShowFilledModulePage() throws Exception {
        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(1L);
        when(module.getName()).thenReturn("MOIA");
        when(module.getResponsible()).thenReturn(teacher);
        PublicationEntity pub1 = mock(PublicationEntity.class), pub2 = mock(PublicationEntity.class);
        when(module.getPublications()).thenReturn(List.of(pub1, pub2));
        when(moduleService.findById(1L)).thenReturn(Optional.of(module));

        UserEntity user = new UserEntity("user@example.com", "hash", "user", Role.PROF, null, null);
        user.setId(1L);
        when(userService.getCurrentUser()).thenReturn(user);
        when(inscriptionService.hasReaderAccess(eq(user), any(ModuleEntity.class))).thenReturn(true);

        mockMvc.perform(get("/modules/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("modules/detail"))
                .andExpect(model().attribute("publications", List.of(pub1, pub2)))
                .andExpect(model().attribute("module", module));
    }

    @Test
    void shouldReturn404WhenModuleDoesNotExist() throws Exception {
        when(moduleService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/modules/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenUserIsNotEnrolled() throws Exception {
        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getResponsible()).thenReturn(teacher);
        when(moduleService.findById(1L)).thenReturn(Optional.of(module));

        UserEntity user = new UserEntity("user@example.com", "hash", "user", Role.PROF, null, null);
        user.setId(1L);
        when(userService.getCurrentUser()).thenReturn(user);
        when(inscriptionService.hasReaderAccess(eq(user), any(ModuleEntity.class))).thenReturn(false);

        mockMvc.perform(get("/modules/1"))
                .andExpect(status().isForbidden());
    }
}