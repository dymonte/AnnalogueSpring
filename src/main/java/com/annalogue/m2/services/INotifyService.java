package com.annalogue.m2.services;

import org.springframework.security.core.AuthenticationException;

public interface INotifyService {

    public String getMessage(String key, Object... args);
    public String getAuthErrorKey(AuthenticationException exception);
    public String getAuthErrorMessage(AuthenticationException exception);
}
