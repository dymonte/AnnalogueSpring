package com.annalogue.m2;

import com.annalogue.m2.controllers.UserController;
import com.annalogue.m2.ldap.SecurityConfig;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.CustomUserDetailsService;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;
import com.annalogue.m2.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class PasswordChangeTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private HandleUserService handleUserService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldShowErrorWhenUserDoesNotExist() throws Exception {

        when(userService.findByEmail("tintin@tin"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/change-password")
                                .with(user("tintin@tin"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("currentPassword", "Oldpass1!")
                                .param("newPassword", "Newpass1!")
                                .param("confirmPassword", "Newpass1!")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("change_password"))
                .andExpect(model().attribute(
                        "error",
                        "Utilisateur introuvable"
                ));

        verify(userService)
                .findByEmail("tintin@tin");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    void shouldShowErrorWhenCurrentPasswordIsWrong() throws Exception {

        UserEntity user = new UserEntity();
        user.setPasswordHash("OLD_HASH");

        when(userService.findByEmail("tintin@tin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "OLD_HASH"
        )).thenReturn(false);

        mockMvc.perform(
                        post("/change-password")
                                .with(user("tintin@tin"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("currentPassword", "WrongPassword")
                                .param("newPassword", "Newpass1!")
                                .param("confirmPassword", "Newpass1!")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("change_password"))
                .andExpect(model().attribute(
                        "currentPasswordError",
                        "Mot de passe actuel incorrect "
                ));

        verify(userService)
                .findByEmail("tintin@tin");

        verify(passwordEncoder)
                .matches("WrongPassword", "OLD_HASH");

        verify(userService, never())
                .save(any(UserEntity.class));
    }


    @Test
    void shouldShowErrorWhenPasswordMismatch() throws Exception {

        UserEntity user = new UserEntity();
        user.setPasswordHash("OLD_HASH");

        when(userService.findByEmail("tintin@tin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "OLD_HASH",
                "OLD_HASH"
        )).thenReturn(true);

        mockMvc.perform(
                        post("/change-password")
                                .with(user("tintin@tin"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("currentPassword", "OLD_HASH")
                                .param("newPassword", "Newpass1!")
                                .param("confirmPassword", "Newpass2!")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("change_password"))
                .andExpect(model().attribute(
                        "confirmPasswordError",
                        "Les mots de passe sont différents "
                ));

        verify(userService)
                .findByEmail("tintin@tin");

        verify(passwordEncoder)
                .matches("OLD_HASH", "OLD_HASH");

        verify(userService, never())
                .save(any(UserEntity.class));
    }

    @Test
    void shouldShowErrorWhenWeakPassword() throws Exception {

        UserEntity user = new UserEntity();
        user.setPasswordHash("OLD_HASH");

        when(userService.findByEmail("tintin@tin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "OLD_HASH",
                "OLD_HASH"
        )).thenReturn(true);

        mockMvc.perform(
                        post("/change-password")
                                .with(user("tintin@tin"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("currentPassword", "OLD_HASH")
                                .param("newPassword", "Newpass1")
                                .param("confirmPassword", "Newpass1")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("change_password"))
                .andExpect(model().attribute(
                        "newPasswordError",
                        "8 caractères, une lettre, un chiffre et un caractère spécial "
                ));

        verify(userService)
                .findByEmail("tintin@tin");

        verify(passwordEncoder)
                .matches("OLD_HASH", "OLD_HASH");

        verify(userService, never())
                .save(any(UserEntity.class));
    }

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        UserEntity user = new UserEntity();
        user.setPasswordHash("OLD_HASH");

        when(userService.findByEmail("tintin@tin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "OLD_HASH",
                "OLD_HASH"
        )).thenReturn(true);

        mockMvc.perform(
                        post("/change-password")
                                .with(user("tintin@tin"))
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .param("currentPassword", "OLD_HASH")
                                .param("newPassword", "Newpass1!")
                                .param("confirmPassword", "Newpass1!")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/change-password"))
                .andExpect(flash().attribute("success", "Mot de passe modifié"));


        verify(userService)
                .findByEmail("tintin@tin");

        verify(passwordEncoder)
                .matches("OLD_HASH", "OLD_HASH");
        verify(passwordEncoder).encode("Newpass1!");
        verify(userService).save(user);
    }
}
