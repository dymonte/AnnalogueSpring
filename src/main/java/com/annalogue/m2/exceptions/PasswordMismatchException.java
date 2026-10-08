package com.annalogue.m2.exceptions;

public class PasswordMismatchException extends RuntimeException {

    public static final String MESSAGE_KEY = "error.password.mismatch";

    public PasswordMismatchException() {
        super(MESSAGE_KEY);
    }
}