package com.annalogue.m2;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {

    private final ResourceBundleMessageSource messageSource = createMessageSource();

    private static ResourceBundleMessageSource createMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }

    @Test
    void shouldContainLoginErrorMessages() {
        assertFalse(message("error.login.email").isBlank());
        assertFalse(message("error.login.password").isBlank());
        assertFalse(message("error.login.disabled").isBlank());
    }

    @Test
    void shouldContainAddMemberMessages() {
        assertFalse(message("success.member.added").isBlank());
        assertFalse(message("error.member.email.exists").isBlank());
    }

    @Test
    void shouldContainFieldValidationMessages() {
        assertFalse(message("error.user.email.null").isBlank());
        assertFalse(message("error.user.email.empty").isBlank());
        assertFalse(message("error.user.email.invalid").isBlank());
        assertFalse(message("error.user.role.empty").isBlank());
        assertFalse(message("error.user.role.invalid").isBlank());
        assertFalse(message("error.user.username.empty").isBlank());
        assertFalse(message("error.user.username.mismatch").isBlank());
        assertFalse(message("error.user.year.empty").isBlank());
        assertFalse(message("error.user.year.invalid").isBlank());
        assertFalse(message("error.user.group.empty").isBlank());
        assertFalse(message("error.user.group.invalid").isBlank());
        assertFalse(message("error.user.role.admin.forbidden").isBlank());
    }

    @Test
    void shouldContainServerErrorMessages() {
        assertFalse(message("error.server").isBlank());
        assertFalse(message("error.database").isBlank());
    }

    @Test
    void shouldInsertEmailInSuccessMessage() {
        String text = messageSource.getMessage(
                "success.member.added",
                new Object[]{"new@test.com"},
                Locale.ENGLISH);

        assertTrue(text.contains("new@test.com"));
    }
}