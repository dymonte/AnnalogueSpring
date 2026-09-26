package com.annalogue.m2.services;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class NotifyService {

    private final MessageSource messageSource;

    public NotifyService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // this part for messages so we can display what already  exist in the messages.properties 
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}