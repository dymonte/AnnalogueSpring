package com.annalogue.m2;

import com.annalogue.m2.controllers.UserController;
import com.annalogue.m2.ldap.SecurityConfig;
import com.annalogue.m2.services.CustomUserDetailsService;
import com.annalogue.m2.services.HandPasswordService;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
        private HandPasswordService HandPasswordService;

        @MockitoBean
        private RoleService roleService;

        @MockitoBean
        private HandleUserService handleUserService;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        void shouldShowErrorWhenUserDoesNotExist() throws Exception {

                doThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException(
                                "Utilisateur introuvable")).when(HandPasswordService).changePassword(
                                                "tintin@tin",
                                                "Oldpass1!",
                                                "Newpass1!",
                                                "Newpass1!");

                mockMvc.perform(
                                post("/change-password")
                                                .with(user("tintin@tin"))
                                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                .param("currentPassword", "Oldpass1!")
                                                .param("newPassword", "Newpass1!")
                                                .param("confirmPassword", "Newpass1!"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("change_password"))
                                .andExpect(model().attribute(
                                                "error",
                                                "Utilisateur introuvable"));

                verify(HandPasswordService).changePassword(
                                "tintin@tin",
                                "Oldpass1!",
                                "Newpass1!",
                                "Newpass1!");
        }

        @Test
        void shouldShowErrorWhenCurrentPasswordIsWrong() throws Exception {

                doThrow(new com.annalogue.m2.exceptions.InvalidPasswordException())
                                .when(HandPasswordService).changePassword(
                                                "tintin@tin",
                                                "WrongPassword",
                                                "Newpass1!",
                                                "Newpass1!");

                mockMvc.perform(
                                post("/change-password")
                                                .with(user("tintin@tin"))
                                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                .param("currentPassword", "WrongPassword")
                                                .param("newPassword", "Newpass1!")
                                                .param("confirmPassword", "Newpass1!"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("change_password"))
                                .andExpect(model().attribute(
                                                "currentPasswordError",
                                                "Mot de passe actuel incorrect "));

                verify(HandPasswordService).changePassword(
                                "tintin@tin",
                                "WrongPassword",
                                "Newpass1!",
                                "Newpass1!");
        }

        @Test
        void shouldShowErrorWhenPasswordMismatch() throws Exception {

                doThrow(new com.annalogue.m2.exceptions.PasswordMismatchException())
                                .when(HandPasswordService).changePassword(
                                                "tintin@tin",
                                                "Oldpass1!",
                                                "Newpass1!",
                                                "Newpass2!");

                mockMvc.perform(
                                post("/change-password")
                                                .with(user("tintin@tin"))
                                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                .param("currentPassword", "Oldpass1!")
                                                .param("newPassword", "Newpass1!")
                                                .param("confirmPassword", "Newpass2!"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("change_password"))
                                .andExpect(model().attribute(
                                                "confirmPasswordError",
                                                "Les mots de passe sont différents "));

                verify(HandPasswordService).changePassword(
                                "tintin@tin",
                                "Oldpass1!",
                                "Newpass1!",
                                "Newpass2!");
        }

        @Test
        void shouldShowErrorWhenWeakPassword() throws Exception {

                doThrow(new com.annalogue.m2.exceptions.WeakPasswordException())
                                .when(HandPasswordService).changePassword(
                                                "tintin@tin",
                                                "Oldpass1!",
                                                "Newpass1",
                                                "Newpass1");

                mockMvc.perform(
                                post("/change-password")
                                                .with(user("tintin@tin"))
                                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                .param("currentPassword", "Oldpass1!")
                                                .param("newPassword", "Newpass1")
                                                .param("confirmPassword", "Newpass1"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("change_password"))
                                .andExpect(model().attribute(
                                                "newPasswordError",
                                                "8 caractères, une lettre, un chiffre et un caractère spécial "));

                verify(HandPasswordService).changePassword(
                                "tintin@tin",
                                "Oldpass1!",
                                "Newpass1",
                                "Newpass1");
        }

        @Test
        void shouldChangePasswordSuccessfully() throws Exception {

                mockMvc.perform(
                                post("/change-password")
                                                .with(user("tintin@tin"))
                                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                .param("currentPassword", "Oldpass1!")
                                                .param("newPassword", "Newpass1!")
                                                .param("confirmPassword", "Newpass1!"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/home"))
                                .andExpect(flash().attribute(
                                                "success",
                                                "Mot de passe modifié"));

                verify(HandPasswordService).changePassword(
                                "tintin@tin",
                                "Oldpass1!",
                                "Newpass1!",
                                "Newpass1!");
        }
}