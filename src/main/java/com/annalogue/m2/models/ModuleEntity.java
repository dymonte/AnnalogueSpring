package com.annalogue.m2.models;

import jakarta.persistence.*;

@Entity
@Table(name = "modules")
public class ModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prof_responsable_id", nullable = false)
    private UserEntity prof_responsable;

    public ModuleEntity() {
    }

    public ModuleEntity(String name, UserEntity prof_responsable) {
        this.name = name;
        this.prof_responsable = prof_responsable;
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

    public UserEntity getProf_responsable() {
        return prof_responsable;
    }

    public void setProf_responsable(UserEntity prof_responsable) {
        this.prof_responsable = prof_responsable;
    }
}