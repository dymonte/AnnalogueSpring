package com.annalogue.m2.exceptions;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super(
                "Les mots de passe sont différents ");
    }
}
