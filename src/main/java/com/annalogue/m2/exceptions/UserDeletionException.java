package com.annalogue.m2.exceptions;

public class UserDeletionException extends RuntimeException {

    private final String messageKey;

    public UserDeletionException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}