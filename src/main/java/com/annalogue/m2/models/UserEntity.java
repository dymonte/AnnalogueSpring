package com.annalogue.m2.models;

import jakarta.persistence.*;

@Entity
@Table(name="user")
public class UserEntity {
    // TODO: complete

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long userId;

    @Column(name = "email")
    String email;

    @Column(name = "password")
    String password;

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
