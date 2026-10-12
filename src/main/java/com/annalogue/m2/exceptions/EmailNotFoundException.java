package com.annalogue.m2.exceptions;

public class EmailNotFoundException extends RuntimeException {
    private final String messageKey;

    public EmailNotFoundException( String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
