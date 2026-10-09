package com.annalogue.m2.models;

import jakarta.persistence.*;

@Entity
@Table(
        name="inscriptions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "module_id"})
        })
public class InscriptionEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InscriptionType inscriptionType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity module;

    private boolean editeur = false;

    public InscriptionEntity() {
    }

    public InscriptionEntity(InscriptionType inscriptionType, UserEntity user, ModuleEntity module) {
        this.inscriptionType = inscriptionType;
        this.user = user;
        this.module = module;
    }

    public Long getId() {
        return id;
    }

    public InscriptionType getInscriptionType() {
        return inscriptionType;
    }

    public void setInscriptionType(InscriptionType inscriptionType) {
        this.inscriptionType = inscriptionType;
    }

    public boolean isEditeur() {
        return editeur;
    }

    public void setEditeur(boolean editeur) {
        this.editeur = editeur;
    }



    public ModuleEntity getModule() {
        return module;
    }

    public void setModule(ModuleEntity module) {
        this.module = module;
    }


    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
