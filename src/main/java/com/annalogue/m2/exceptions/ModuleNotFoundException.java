package com.annalogue.m2.exceptions;

public class ModuleNotFoundException extends RuntimeException {
    private final String messsageKey;
    public ModuleNotFoundException(String messsageKey) {

        super(messsageKey);
        this.messsageKey = messsageKey;
    }
    public String getMessageKey() {
        return messsageKey;
    }
}
