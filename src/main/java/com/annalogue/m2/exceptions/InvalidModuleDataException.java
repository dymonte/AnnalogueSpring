package com.annalogue.m2.exceptions;

public class InvalidModuleDataException extends RuntimeException {

    private final String messageKey;

    public InvalidModuleDataException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}