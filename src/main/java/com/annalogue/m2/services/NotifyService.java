package com.annalogue.m2.services;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.context.NoSuchMessageException;
@Service
public class NotifyService {

    public static final String DEFAULT_ERROR_KEY = "error.server";

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
}