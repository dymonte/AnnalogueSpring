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

import com.annalogue.m2.services.NotifyService;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, NotifyService.class})
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
                                "error.password.user.notfound")).when(HandPasswordService).changePassword(
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
                                                "User not found."));

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
                                                "The current password is incorrect."));

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
                                                "The two passwords do not match."));

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
                                                "The password must contain at least 8 characters, one letter, one digit and one special character."));

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
                                                "Your password has been changed."));

                verify(HandPasswordService).changePassword(
                                "tintin@tin",
                                "Oldpass1!",
                                "Newpass1!",
                                "Newpass1!");
        }
}