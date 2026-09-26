package com.annalogue.m2.exceptions;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException()
    {
        super(
                "8 caractères, une lettre, un chiffre et un caractère spécial "
        );
    }
}
