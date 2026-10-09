
package com.annalogue.m2.models;

public enum InscriptionType {
    ETUDIANT("étudiant"),
    INTERVENANT("intervenant");

    private final String value;

    InscriptionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
