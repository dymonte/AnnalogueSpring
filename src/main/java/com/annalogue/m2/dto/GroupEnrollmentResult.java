package com.annalogue.m2.dto;

import java.util.List;

public record GroupEnrollmentResult(int added, int alreadyRegistered, List<String> alreadyRegisteredNames) {

    public GroupEnrollmentResult(int added, int alreadyRegistered) {
        this(added, alreadyRegistered, List.of());
    }
}