package com.annalogue.m2.models;

import jakarta.persistence.*;

import java.io.File;

@Entity
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private UserEntity authorComment;

    @ManyToOne
    @JoinColumn(name = "publication_id")
    private PublicationEntity publication;

    @ManyToOne
    @JoinColumn(name = "correction_id")
    private CorrectionEntity correction;

    @Column(name = "path", nullable = true)
    private String path;

    public CommentEntity() {}

    public CommentEntity(String content) {
        this.content = content;
    }

    public CommentEntity(String content, UserEntity author, PublicationEntity publication, String path) {
        this.content = content;
        this.authorComment = author;
        this.publication = publication;
        this.path = path;
    }

    public CommentEntity(String content, UserEntity author, CorrectionEntity correction, String path) {
        this.content = content;
        this.authorComment = author;
        this.correction = correction;
        this.path = path;
    }

    public void setAuthorComment(UserEntity authorComment) {
        this.authorComment = authorComment;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPublication(PublicationEntity publication) {
        this.publication = publication;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public UserEntity getAuthorComment() {
        return authorComment;
    }

    public PublicationEntity getPublication() {
        return publication;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CorrectionEntity getCorrection() {
        return correction;
    }

    public void setCorrection(CorrectionEntity correction) {
        this.correction = correction;
    }
}
