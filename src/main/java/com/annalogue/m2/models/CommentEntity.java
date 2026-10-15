package com.annalogue.m2.models;

import jakarta.persistence.*;

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

    public CommentEntity() {}

    public CommentEntity(String content) {
        this.content = content;
    }

    public CommentEntity(String content, UserEntity author, PublicationEntity publication) {
        this.content = content;
        this.authorComment = author;
        this.publication = publication;
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
}
