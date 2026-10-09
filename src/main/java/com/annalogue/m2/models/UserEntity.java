package com.annalogue.m2.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "year_level")
    private Integer year;


    @Enumerated(EnumType.STRING)
    @Column(name = "study_level", length = 2)
    private StudyLevel level;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @OneToMany(mappedBy = "prof_responsable")
    private List<ModuleEntity> modulesResponsable;

    // JPA constructor
    public UserEntity() {
    }

    public UserEntity(String email, String passwordHash, String username,
            Role role, Integer year, String groupName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
        this.role = role;
        this.year = year;
        this.groupName = groupName;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public StudyLevel getLevel() {
        return level;
    }

    public void setLevel(StudyLevel level) {
        this.level = level;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}