package com.annalogue.m2.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }

        return role.getValue();
    }

    @Override
    public Role convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        for (Role role : Role.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }

        throw new IllegalArgumentException(
                "Rôle inconnu : " + value);
    }
}