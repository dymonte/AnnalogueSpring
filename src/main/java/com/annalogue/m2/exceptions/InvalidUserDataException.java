package com.annalogue.m2.exceptions;

public class InvalidUserDataException extends RuntimeException {

    private final String field;
    private final String messageKey;

    public InvalidUserDataException(String field, String messageKey) {
        super("Invalid value for field: " + field);
        this.field = field;
        this.messageKey = messageKey;
    }

    public String getField() {
        return field;
    }

    public String getMessageKey() {
        return messageKey;
    }
}