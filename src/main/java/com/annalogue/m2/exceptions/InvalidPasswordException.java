package com.annalogue.m2.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {

        super("Mot de passe actuel incorrect ");
    }
}
