package com.annalogue.m2.models;

import jakarta.persistence.*;

@Entity
@Table(name = "modules")
public class ModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // a module name is required and unique (e.g. "MOIA", "TF")
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    // JPA constructor
    public ModuleEntity() {
    }

    public ModuleEntity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}