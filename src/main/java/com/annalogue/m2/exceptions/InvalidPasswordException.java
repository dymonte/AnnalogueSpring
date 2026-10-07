package com.annalogue.m2.exceptions;

// the message is a key of messages.properties
public class InvalidPasswordException extends RuntimeException {

    public static final String MESSAGE_KEY = "error.password.current.invalid";

    public InvalidPasswordException() {
        super(MESSAGE_KEY);
    }
}