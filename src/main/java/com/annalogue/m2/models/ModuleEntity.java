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
    @JoinColumn(name = "responsible_id", nullable = false)
    private UserEntity responsable;

    public ModuleEntity() {
    }

    public ModuleEntity(String name, UserEntity responsable) {
        this.name = name;
        this.responsable =responsable;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserEntity getResponsable() {
        return responsable;
    }

    public void setResponsable(UserEntity responsable) {
        this.responsable = responsable;
    }
}