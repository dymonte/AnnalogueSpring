package com.annalogue.m2.models;

import java.util.Arrays;

// level of a student: L1, L2, L3, M1, M2
public enum StudyLevel {
    L1, L2, L3, M1, M2;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(level -> level.name().equalsIgnoreCase(value.trim()));
    }

    public static StudyLevel fromString(String value) {
        return StudyLevel.valueOf(value.trim().toUpperCase());
    }
}