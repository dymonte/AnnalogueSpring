package com.annalogue.m2.dto;

// result of the registration of a whole group in a module
public record GroupEnrollmentResult(int added, int alreadyRegistered) {
}