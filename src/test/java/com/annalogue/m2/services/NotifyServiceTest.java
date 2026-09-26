package com.annalogue.m2.services;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifyServiceTest {

    private final NotifyService notifyService = new NotifyService(createMessageSource());

    private static ResourceBundleMessageSource createMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

   

    @Test
    void shouldReturnMessageForKey() {
        assertEquals("Incorrect email or password.",
                notifyService.getMessage("error.login.generic"));
    }

    @Test
    void shouldInsertArgumentsInMessage() {
        String message = notifyService.getMessage("success.member.added", "new@test.com");

        assertTrue(message.contains("new@test.com"));
    }
}