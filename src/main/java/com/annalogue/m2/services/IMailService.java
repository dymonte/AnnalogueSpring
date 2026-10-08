package com.annalogue.m2.services;

public interface IMailService {
    public void sendCredentials(String email, String username, String temporaryPassword);

}
