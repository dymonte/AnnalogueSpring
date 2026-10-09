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
        assertFalse(message("error.user.username.invalid").isBlank());
        assertFalse(message("error.user.level.empty").isBlank());
        assertFalse(message("error.user.level.invalid").isBlank());
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

    @Test
    void shouldContainCentralizedMessagesForTfan2698() {
        assertFalse(message("success.password.changed").isBlank());
        assertFalse(message("error.password.user.notfound").isBlank());
        assertFalse(message("error.password.current.invalid").isBlank());
        assertFalse(message("error.password.mismatch").isBlank());
        assertFalse(message("error.password.weak").isBlank());
        assertFalse(message("success.logout").isBlank());
        assertFalse(message("error.page.notfound").isBlank());
        assertFalse(message("error.page.forbidden").isBlank());
        assertFalse(message("error.page.server").isBlank());
        assertFalse(message("info.user.username.available").isBlank());
        assertFalse(message("warning.user.username.taken").isBlank());
        assertFalse(message("action.user.username.use").isBlank());
        assertFalse(message("mail.credentials.subject").isBlank());
    }

    @Test
    void shouldContainPageTexts() {
        assertFalse(message("nav.home").isBlank());
        assertFalse(message("role.PROF").isBlank());
        assertFalse(message("role.ETUDIANT").isBlank());
        assertFalse(message("login.submit").isBlank());
        assertFalse(message("home.change.password").isBlank());
        assertFalse(message("password.submit").isBlank());
        assertFalse(message("member.submit").isBlank());
        assertFalse(message("error.page.heading").isBlank());
        assertFalse(message("action.close").isBlank());
    }
    @Test
    void shouldContainDeleteMemberMessages() {
        assertFalse(message("members.title").isBlank());
        assertFalse(message("members.delete").isBlank());
        assertFalse(message("members.delete.confirm").isBlank());
        assertFalse(message("success.member.deleted").isBlank());
        assertFalse(message("error.user.delete.notfound").isBlank());
        assertFalse(message("error.user.delete.admin").isBlank());
        assertFalse(message("error.user.delete.linked").isBlank());
        assertFalse(message("error.user.delete.database").isBlank());
    }

    @Test
    void shouldContainModuleControllerMessages() {
        assertFalse(message("error.module.name.empty").isBlank());
        assertFalse(message("error.module.name.tooLong").isBlank());
        assertFalse(message("error.module.name.exists").isBlank());
        assertFalse(message("error.module.database").isBlank());
        assertFalse(message("success.module.created").isBlank());
    }
}