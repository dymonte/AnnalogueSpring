package com.annalogue.m2.models;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "hash_mdp", nullable = false)
    private String hashMdp;

    @Column(name = "identifiant", length = 100)
    private String identifiant;

    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "annee")
    private String annee;

    @Column(name = "groupe", length = 100)
    private String groupe;

    // Constructeur JPA
    public UserEntity() {
    }

    public UserEntity(String email, String hashMdp, String identifiant,
            Role role, String annee, String groupe) {
        this.email = email;
        this.hashMdp = hashMdp;
        this.identifiant = identifiant;
        this.role = role;
        this.annee = annee;
        this.groupe = groupe;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashMdp() {
        return hashMdp;
    }

    public void setHashMdp(String hashMdp) {
        this.hashMdp = hashMdp;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }
}