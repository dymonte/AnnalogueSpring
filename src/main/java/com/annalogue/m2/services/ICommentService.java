package com.annalogue.m2.services;

import com.annalogue.m2.models.CommentEntity;

public interface ICommentService {
    public CommentEntity addComment(String content, long userId, long publicationId);
    public void deleteComment(long idComment);
}
