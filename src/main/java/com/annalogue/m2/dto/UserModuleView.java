package com.annalogue.m2.dto;

// one line of « My modules »: a module and the place of the user in it
public class UserModuleView {

    // label of the modules the user created (he is their responsible)
    public static final String RESPONSIBLE_ROLE_KEY = "my.modules.role.responsible";

    private final Long id;
    private final String name;
    // message key of the user's label in the module (my.modules.role.*); null for the admin
    private final String roleKey;
    // true if the user is enrolled with the editor right
    private final boolean editor;

    public UserModuleView(Long id, String name, String roleKey, boolean editor) {
        this.id = id;
        this.name = name;
        this.roleKey = roleKey;
        this.editor = editor;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public boolean isEditor() {
        return editor;
    }

    // true for the modules the user created
    public boolean isResponsible() {
        return RESPONSIBLE_ROLE_KEY.equals(roleKey);
    }
}