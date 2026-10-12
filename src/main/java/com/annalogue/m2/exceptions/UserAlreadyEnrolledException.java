package com.annalogue.m2.exceptions;

public class UserAlreadyEnrolledException extends RuntimeException {
    private final String messageKey;
    public UserAlreadyEnrolledException(String messageKey) {

        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
