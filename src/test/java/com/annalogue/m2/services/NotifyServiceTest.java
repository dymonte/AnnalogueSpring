package com.annalogue.m2.services;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

    @Test
    void shouldReturnServerErrorForUnknownKey() {
        assertEquals("An error occurred. Please try again later.",
                notifyService.getMessage("this.key.does.not.exist"));
    }

    // for the login now 

    @Test
    void shouldUseGenericMessageForWrongPassword() {
        assertEquals("error.login.generic",
                notifyService.getAuthErrorKey(new BadCredentialsException("Bad credentials")));
    }

    @Test
    void shouldUseGenericMessageForUnknownEmail() {
        assertEquals("error.login.generic",
                notifyService.getAuthErrorKey(new UsernameNotFoundException("User not found")));
    }

    @Test
    void shouldUseDisabledMessageForDisabledAccount() {
        assertEquals("error.login.disabled",
                notifyService.getAuthErrorKey(new DisabledException("Account disabled")));
    }

    @Test
    void shouldReturnAuthErrorText() {
        assertEquals("Incorrect email or password.",
                notifyService.getAuthErrorMessage(new BadCredentialsException("Bad credentials")));
    }
}