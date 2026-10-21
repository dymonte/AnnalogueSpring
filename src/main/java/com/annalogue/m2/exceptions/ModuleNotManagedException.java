package com.annalogue.m2.exceptions;

public class ModuleNotManagedException extends RuntimeException {
    private final String messageKey;

    public ModuleNotManagedException(String messageKey) {

        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
