package com.annalogue.m2.models;

public enum Role {

    ADMIN("admin"),
    RESPONSABLE("responsable"),
    PROF("prof"),
    ETUDIANT("étudiant");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}