package com.annalogue.m2.services;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.context.NoSuchMessageException;

import org.springframework.security.core.AuthenticationException;

import org.springframework.security.authentication.DisabledException;

@Service
public class NotifyService implements INotifyService{

    public static final String DEFAULT_ERROR_KEY = "error.server";
    public static final String LOGIN_GENERIC_KEY = "error.login.generic";
    public static final String LOGIN_DISABLED_KEY = "error.login.disabled";

    private final MessageSource messageSource;

    public NotifyService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // this part for messages so we can display what already  exist in the messages.properties 
    // update the logic so the unknown key will be  a part of message not server error 

    public String getMessage(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            // unknown key: show the general error message instead of crashing
            return messageSource.getMessage(DEFAULT_ERROR_KEY, null, LocaleContextHolder.getLocale());
        }
    }

    public String getAuthErrorKey(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return LOGIN_DISABLED_KEY;
        }
        // wrong password and unknown email: same message on purpose we can seperated if u want
        return LOGIN_GENERIC_KEY;
    }

    public String getAuthErrorMessage(AuthenticationException exception) {
        return getMessage(getAuthErrorKey(exception));
    }
}