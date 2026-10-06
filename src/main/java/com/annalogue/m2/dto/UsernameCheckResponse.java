package com.annalogue.m2.dto;

// answer of the instant username check: is it free, and which name to suggest
public record UsernameCheckResponse(boolean available, String suggestion) {
}