package com.annalogue.m2.models;

import jakarta.persistence.*;

@Entity
public class CorrectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private UserEntity authorComment;

    @ManyToOne
    @JoinColumn(name = "publication_id")
    private PublicationEntity publication;

    @Column(name = "path")
    private String path;

    public CorrectionEntity() {
    }

    public CorrectionEntity(
            UserEntity author,
            PublicationEntity publication,
            String title,
            String path) {
        this.authorComment = author;
        this.publication = publication;
        this.title = title;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserEntity getAuthorComment() {
        return authorComment;
    }

    public void setAuthorComment(UserEntity authorComment) {
        this.authorComment = authorComment;
    }

    public PublicationEntity getPublication() {
        return publication;
    }

    public void setPublication(PublicationEntity publication) {
        this.publication = publication;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}