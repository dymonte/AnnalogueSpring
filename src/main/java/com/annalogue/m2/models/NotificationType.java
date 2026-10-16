package com.annalogue.m2.models;

public enum NotificationType {
    ACCOUNT_CREATED,    
    MODULE_ENROLLMENT, 
    PUBLICATION_ADDED;  

    public String getMessageKey() {
        return "notification." + name();
    }
}