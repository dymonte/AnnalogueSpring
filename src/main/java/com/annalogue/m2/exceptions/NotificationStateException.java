package com.annalogue.m2.exceptions;

public class NotificationStateException extends RuntimeException {

    private final String messageKey;

    public NotificationStateException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}