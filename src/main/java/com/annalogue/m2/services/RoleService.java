package com.annalogue.m2.services;

import com.annalogue.m2.models.Role;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RoleService {

    public List<Role> getRoles() {
        return Arrays.asList(Role.values());
    }

    public boolean exists(String role) {
        return Arrays.stream(Role.values())
                .anyMatch(r -> r.name().equalsIgnoreCase(role));
    }

    public Role fromString(String role) {
        return Arrays.stream(Role.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found : " + role));
    }

   // roles the admin is allowed to give: everything except ADMIN
    public List<Role> getAssignableRoles() {
        return Arrays.stream(Role.values())
                .filter(role -> role != Role.ADMIN)
                .toList();
    }
    // roles a teacher can switch between (Prof <-> Responsable Module)
    public List<Role> getTeacherRoles() {
        return List.of(Role.PROF, Role.RESPONSABLE);
    }
}
