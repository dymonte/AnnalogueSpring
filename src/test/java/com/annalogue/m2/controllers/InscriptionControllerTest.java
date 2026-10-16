package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.EmailNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotManagedException;
import com.annalogue.m2.exceptions.UserAlreadyEnrolledException;
import com.annalogue.m2.ldap.SecurityConfig;
import com.annalogue.m2.models.*;
import com.annalogue.m2.services.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InscriptionController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class InscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private IInscriptionService inscriptionService;

    @MockitoBean
    private NotifyService notifyService;

    private UserEntity fmaiou;
    private UserEntity achevlot;
    private UserEntity fbouquet;

    private ModuleEntity tf;
    private ModuleEntity java;
    private ModuleEntity rx;
    private InscriptionEntity tf_fmaiou;
    private InscriptionEntity tf_achevlot;
    private InscriptionEntity rx_fmaiou;
    @BeforeEach
    void setUp() {
        fmaiou = new UserEntity(
                "fmaiou@ufr.com",
                "fake-hash",
                "fmaiou",
                Role.ETUDIANT,
                2026,
                "G1"
        );
        fmaiou.setId(2L);

        achevlot = new UserEntity(
                "achevlot@ufr.com",
                "fake-hash",
                "achevlot",
                Role.PROF,
                null,
                null
        );
        achevlot.setId(3L);

        fbouquet = new UserEntity(
                "fbouquet@ufr.com",
                "fake-hash",
                "fbouquet",
                Role.PROF,
                null,
                null
        );
        fbouquet.setId(1L);

        tf = new ModuleEntity("TF", fbouquet);
        tf.setId(10L);

        java = new ModuleEntity("Java", fbouquet);
        java.setId(11L);

        rx = new ModuleEntity("RX", achevlot);
        rx.setId(12L);

        achevlot.setResponsibleModules(List.of(rx));
        fbouquet.setResponsibleModules(List.of(tf, java));

        tf_fmaiou = new InscriptionEntity(InscriptionType.ETUDIANT, fmaiou, tf);tf_fmaiou.setId(1L);
        tf_achevlot = new InscriptionEntity(InscriptionType.INTERVENANT, achevlot, tf);tf_achevlot.setId(2L);
        rx_fmaiou = new InscriptionEntity(InscriptionType.ETUDIANT, fmaiou, rx);rx_fmaiou.setId(3L);
    }

    // GET requests

    @Test
    void shouldReturnRegisterMemberToModulePage()
            throws Exception {

        when(userService.findByEmail(fbouquet.getEmail()))
                .thenReturn(Optional.of(fbouquet));

        mockMvc.perform(
                        get("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attributeExists("modules"))
                .andExpect(model().attribute(
                        "modules",
                        containsInAnyOrder(tf, java)
                ))
                .andExpect(model().attribute(
                        "modules",
                        not(hasItem(rx))
                ));
    }

    @Test
    void shouldDenyStudentAccessToRegisterPageGet() throws Exception {
        mockMvc.perform(
                        get("/module-enrollments/new")
                                .with(user("fmaiou@ufr.com").roles("ETUDIANT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToRegisterPageGet()
            throws Exception {

        mockMvc.perform(get("/module-enrollments/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));
    }

    @Test
    void shouldShowEmptyModulesWhenAuthenticatedUserDoesNotExist()
            throws Exception {

        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.empty());

        when(notifyService.getMessage(
                "error.inscription.prof.not.found"
        )).thenReturn("Professor not found.");

        mockMvc.perform(
                        get("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attribute("modules", List.of()))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void shouldReturnRegisterMembersViewPage() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com")).thenReturn(Optional.of(fbouquet));;
        when(inscriptionService.findInscriptions(fbouquet)).thenReturn(List.of(tf_achevlot, tf_fmaiou));
        mockMvc.perform(
                get("/module-enrollments")
                        .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))

        ).andExpect(status().isOk())
                .andExpect(view().name("modules/register_members_view"))
                .andExpect(model().attributeExists("inscriptions"))
                .andExpect(model().attribute(
                        "inscriptions",
                        containsInAnyOrder(tf_achevlot, tf_fmaiou)
                ));
    }

    @Test
    void shouldDenyUnauthenticatedAccessToRegisterMembersView() throws Exception {
        mockMvc.perform(
                        get("/module-enrollments")

                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));
    }

    @Test
    void shouldDenyUnautorizedAccessToRegisterMembersView() throws Exception {
        mockMvc.perform(
                        get("/module-enrollments")
                                .with(user("fmaiou@ufr.com").roles("ETUDIANT"))

                ).andExpect(status().isForbidden());
    }

    // POST requests

    @Test
    void shouldRegisterUserToModule() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        when(notifyService.getMessage("success.inscription.created"))
                .thenReturn("User successfully registered.");

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "10")
                                .param("emailUser", "fmaiou@ufr.com")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"));

        verify(inscriptionService).save(
                fbouquet,
                "fmaiou@ufr.com",
                10L
        );

        verify(notifyService).getMessage("success.inscription.created");
    }

    @Test
    void shouldShowErrorWhenUserDoesNotExist() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        doThrow(new EmailNotFoundException(
                "error.inscription.user.not.found"
        )).when(inscriptionService)
                .save(fbouquet, "inconnu@ufr.com", 10L);

        when(notifyService.getMessage(
                "error.inscription.user.not.found"
        )).thenReturn("No user was found with this email address.");

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "10")
                                .param("emailUser", "inconnu@ufr.com")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attributeExists("emailUserError"))
                .andExpect(model().attribute(
                        "emailUserError",
                        "No user was found with this email address."
                ))
                .andExpect(model().attribute("selectedModuleId", 10L))
                .andExpect(model().attribute(
                        "emailUser",
                        "inconnu@ufr.com"
                ));

        verify(inscriptionService).save(
                fbouquet,
                "inconnu@ufr.com",
                10L
        );
    }

    @Test
    void shouldThrowExceptionWhenModuleNotFound() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        doThrow(new ModuleNotFoundException(
                "error.inscription.module.not.found"
        )).when(inscriptionService)
                .save(fbouquet, "fmaiou@ufr.com", 9L);

        when(notifyService.getMessage(
                "error.inscription.module.not.found"
        )).thenReturn("The selected module does not exist.");

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "9")
                                .param("emailUser", "fmaiou@ufr.com")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attributeExists("moduleIdError"))
                .andExpect(model().attribute(
                        "moduleIdError",
                        "The selected module does not exist."
                ))
                .andExpect(model().attribute("selectedModuleId", 9L))
                .andExpect(model().attribute(
                        "emailUser",
                        "fmaiou@ufr.com"
                ));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyEnrolled() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        doThrow(new UserAlreadyEnrolledException(
                "error.inscription.user.already.registered"
        )).when(inscriptionService)
                .save(fbouquet, "fmaiou@ufr.com", 10L);

        when(notifyService.getMessage(
                "error.inscription.user.already.registered"
        )).thenReturn("This user is already registered for this module.");

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "10")
                                .param("emailUser", "fmaiou@ufr.com")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attributeExists(
                        "userAlreadyRegisterError"
                ))
                .andExpect(model().attribute(
                        "userAlreadyRegisterError",
                        "This user is already registered for this module."
                ))
                .andExpect(model().attribute("selectedModuleId", 10L))
                .andExpect(model().attribute(
                        "emailUser",
                        "fmaiou@ufr.com"
                ));
    }

    @Test
    void shouldThrowExceptionWhenModuleNotManaged() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        doThrow(new ModuleNotManagedException(
                "error.inscription.module.not.managed"
        )).when(inscriptionService)
                .save(fbouquet, "fmaiou@ufr.com", 12L);

        when(notifyService.getMessage(
                "error.inscription.module.not.managed"
        )).thenReturn("You are not authorized to manage this module.");

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "12")
                                .param("emailUser", "fmaiou@ufr.com")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attributeExists("moduleIdError"))
                .andExpect(model().attribute(
                        "moduleIdError",
                        "You are not authorized to manage this module."
                ))
                .andExpect(model().attribute("selectedModuleId", 12L))
                .andExpect(model().attribute(
                        "emailUser",
                        "fmaiou@ufr.com"
                ));
    }

    @Test
    void shouldThrowExceptionWhenModuleIdIsMissing() throws Exception {
        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("emailUser", "fmaiou@ufr.com")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowExceptionWhenEmailUserIsMissing() throws Exception {
        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "10")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowExceptionWhenUserEmailIsBlank() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        doThrow(new IllegalArgumentException(
                "error.inscription.email.required"
        )).when(inscriptionService)
                .save(fbouquet, "", 10L);

        when(notifyService.getMessage(
                "error.inscription.email.required"
        )).thenReturn("Please enter the user's email address.");

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "10")
                                .param("emailUser", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attributeExists("emailUserError"))
                .andExpect(model().attribute(
                        "emailUserError",
                        "Please enter the user's email address."
                ))
                .andExpect(model().attribute("selectedModuleId", 10L))
                .andExpect(model().attribute("emailUser", ""));
    }

    @Test
    void shouldThrowExceptionWhenUserEmailIsInvalid() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        doThrow(new IllegalArgumentException(
                "error.inscription.email.invalid"
        )).when(inscriptionService)
                .save(fbouquet, "fmaiou.com", 10L);

        when(notifyService.getMessage(
                "error.inscription.email.invalid"
        )).thenReturn("Please enter a valid email address.");

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                                .param("moduleId", "10")
                                .param("emailUser", "fmaiou.com")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("modules/add_member_module"))
                .andExpect(model().attributeExists("emailUserError"))
                .andExpect(model().attribute(
                        "emailUserError",
                        "Please enter a valid email address."
                ))
                .andExpect(model().attribute("selectedModuleId", 10L))
                .andExpect(model().attribute("emailUser", "fmaiou.com"));
    }

    @Test
    void shouldDenyStudentAccessToRegisterPagePost() throws Exception {
        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(user("fmaiou@ufr.com").roles("ETUDIANT"))
                                .with(csrf())
                                .param("moduleId", "10")
                                .param("emailUser", "fmaiou@ufr.com")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToRegisterPagePost() throws Exception {

        mockMvc.perform(
                        post("/module-enrollments/new")
                                .with(csrf())
                                .param("moduleId", "10")
                                .param("emailUser", "fmaiou@ufr.com")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));
    }

    @Test
    void shouldRemoveEnrollmentById() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        when(inscriptionService.findInscriptions(fbouquet))
                .thenReturn(List.of(tf_achevlot, tf_fmaiou));

        Long id = tf_achevlot.getId();

        when(inscriptionService.existsById(id)).thenReturn(true);

        mockMvc.perform(
                        post("/module-enrollments/{id}/delete", id)
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"));

        verify(inscriptionService).deleteById(id);
    }

    @Test
    void shouldDenyUnauthenticatedToRemoveEnrollmentById() throws Exception {
        Long id = tf_achevlot.getId();
        mockMvc.perform(
                        post("/module-enrollments/{id}/delete", id)
                                .with(csrf())
             )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));

        verify(inscriptionService, never()).deleteById(id);
    }

    @Test
    void shouldDenyStudentAccessToRemoveEnrollmentById() throws Exception {
        when(userService.findByEmail("fmaiou@ufr.com"))
                .thenReturn(Optional.of(fbouquet));;
        Long id = tf_achevlot.getId();
        mockMvc.perform(
                        post("/module-enrollments/{id}/delete", id)
                                .with(user("fmaiou@ufr.com").roles("ETUDIANT"))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verify(inscriptionService, never()).deleteById(id);
    }

    @Test
    void shouldDenyRemoveEnrollmentByIdWhenInscriptionModuleNotManaged() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        when(inscriptionService.findInscriptions(fbouquet))
                .thenReturn(List.of(tf_achevlot, tf_fmaiou));

        Long id = rx_fmaiou.getId();

        when(inscriptionService.existsById(id)).thenReturn(true);
        when(notifyService.getMessage("error.inscription.not.found")).thenReturn(
                "Enrollment not found or you are not authorized to delete it."
        );
        mockMvc.perform(
                        post("/module-enrollments/{id}/delete", id)
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Enrollment not found or you are not authorized to delete it."));

        verify(inscriptionService, never()).deleteById(id);
    }

    @Test
    void shouldDenyRemoveEnrollmentByIdWhenInscriptionModuleNotExiste() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        when(inscriptionService.findInscriptions(fbouquet))
                .thenReturn(List.of(tf_achevlot, tf_fmaiou));

        when(inscriptionService.existsById(5L)).thenReturn(false);
        when(notifyService.getMessage("error.inscription.not.found")).thenReturn(
                "Enrollment not found or you are not authorized to delete it."
        );
        mockMvc.perform(
                        post("/module-enrollments/{id}/delete", 5L)
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Enrollment not found or you are not authorized to delete it."));

        verify(inscriptionService, never()).deleteById(5L);
    }

    @Test
    void shouldEnableEditor() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        when(inscriptionService.findInscriptions(fbouquet)).thenReturn(
                List.of(tf_achevlot, tf_fmaiou)
        );
        when(inscriptionService.existsById(2L)).thenReturn(true);
        when(notifyService.getMessage("success.inscription.editor.enabled")).thenReturn("Editor status enabled successfully.");
        when(notifyService.getMessage("success.inscription.editor.disabled")).thenReturn("Editor status disabled successfully.");

        mockMvc.perform(
                post("/module-enrollments/{id}/toggle-editor", tf_achevlot.getId())
                        .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                        .with(csrf())

        ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", tf_achevlot.isEditor()?"Editor status enabled successfully." : "Editor status disabled successfully."));

        verify(inscriptionService).findInscriptions(fbouquet);
        verify(inscriptionService).existsById(2L);
        verify(inscriptionService).toggleEditor(2L);
    }

    @Test
    void shouldDenyUnauthenticatedToToggleEditor() throws Exception {
        Long id = tf_achevlot.getId();
        mockMvc.perform(
                        post("/module-enrollments/{id}/toggle-editor", id)
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));

        verify(inscriptionService, never()).toggleEditor(id);
    }

    @Test
    void shouldDenyStudentAccessToToggleEditor() throws Exception {
        when(userService.findByEmail("fmaiou@ufr.com"))
                .thenReturn(Optional.of(fbouquet));;
        Long id = tf_achevlot.getId();
        mockMvc.perform(
                        post("/module-enrollments/{id}/toggle-editor", id)
                                .with(user("fmaiou@ufr.com").roles("ETUDIANT"))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verify(inscriptionService, never()).toggleEditor(id);
    }

    @Test
    void shouldDenyToggleEditorWhenInscriptionModuleNotManaged() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        when(inscriptionService.findInscriptions(fbouquet))
                .thenReturn(List.of(tf_achevlot, tf_fmaiou));

        Long id = rx_fmaiou.getId();

        when(inscriptionService.existsById(id)).thenReturn(true);
        when(notifyService.getMessage("error.inscription.not.found")).thenReturn(
                "Enrollment not found or you are not authorized to modify or delete it."
        );
        mockMvc.perform(
                        post("/module-enrollments/{id}/toggle-editor", id)
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Enrollment not found or you are not authorized to modify or delete it."));

        verify(inscriptionService, never()).toggleEditor(id);
    }

    @Test
    void shouldDenyToggleEditorWhenInscriptionModuleNotExiste() throws Exception {
        when(userService.findByEmail("fbouquet@ufr.com"))
                .thenReturn(Optional.of(fbouquet));

        when(inscriptionService.findInscriptions(fbouquet))
                .thenReturn(List.of(tf_achevlot, tf_fmaiou));

        when(inscriptionService.existsById(5L)).thenReturn(false);
        when(notifyService.getMessage("error.inscription.not.found")).thenReturn(
                "Enrollment not found or you are not authorized to modify or delete it."
        );
        mockMvc.perform(
                        post("/module-enrollments/{id}/delete", 5L)
                                .with(user("fbouquet@ufr.com").roles("RESPONSABLE"))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/module-enrollments"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Enrollment not found or you are not authorized to modify or delete it."));

        verify(inscriptionService, never()).toggleEditor(5L);
    }

    @Test
    void shouldDenyProfessorAccessToRegisterPageGet() throws Exception {
        mockMvc.perform(
                        get("/module-enrollments/new")
                                .with(user("achevlot@ufr.com").roles("PROF"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyProfessorAccessToToggleEditor() throws Exception {
        Long id = tf_achevlot.getId();
        mockMvc.perform(
                        post("/module-enrollments/{id}/toggle-editor", id)
                                .with(user("achevlot@ufr.com").roles("PROF"))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verify(inscriptionService, never()).toggleEditor(id);
    }
}