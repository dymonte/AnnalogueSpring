package com.annalogue.m2.services;

import com.annalogue.m2.models.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleServiceTest {

    private final RoleService roleService = new RoleService();

    @Test
    void shouldReturnAllRoles() {

        List<Role> roles = roleService.getRoles();

        assertEquals(4, roles.size());
        assertTrue(roles.contains(Role.ADMIN));
        assertTrue(roles.contains(Role.RESPONSABLE));
        assertTrue(roles.contains(Role.PROF));
        assertTrue(roles.contains(Role.ETUDIANT));
    }

    @Test
    void shouldAcceptExistingRolesIgnoringCase() {

        assertTrue(roleService.exists("ADMIN"));
        assertTrue(roleService.exists("Responsable"));
        assertTrue(roleService.exists("prof"));
        assertTrue(roleService.exists("Etudiant"));
    }

    @Test
    void shouldRejectUnknownRole() {

        assertFalse(roleService.exists("directeur"));
    }

    @Test
    void shouldConvertStringToRole() {

        assertEquals(Role.ADMIN, roleService.fromString("admin"));
        assertEquals(Role.PROF, roleService.fromString("PROF"));
        assertEquals(Role.ETUDIANT, roleService.fromString("etudiant"));
    }

    @Test
    void shouldThrowExceptionForUnknownRoleString() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.fromString("directeur"));
    }
    @Test
    void assignableRolesShouldNotContainAdmin() {

        List<Role> roles = roleService.getAssignableRoles();

        assertEquals(3, roles.size());
        assertTrue(roles.contains(Role.RESPONSABLE));
        assertTrue(roles.contains(Role.PROF));
        assertTrue(roles.contains(Role.ETUDIANT));
        assertFalse(roles.contains(Role.ADMIN));
    }

    @Test
    void teacherRolesShouldBeProfAndResponsable() {

        List<Role> roles = roleService.getTeacherRoles();

        assertEquals(List.of(Role.PROF, Role.RESPONSABLE), roles);
    }
}