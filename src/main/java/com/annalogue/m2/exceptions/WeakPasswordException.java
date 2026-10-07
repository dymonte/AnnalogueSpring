package com.annalogue.m2.exceptions;

public class WeakPasswordException extends RuntimeException {

    public static final String MESSAGE_KEY = "error.password.weak";

    public WeakPasswordException() {
        super(MESSAGE_KEY);
    }
}