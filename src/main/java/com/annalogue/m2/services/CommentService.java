package com.annalogue.m2.services;

import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final IUserService userService;
    private final IPublicationService publicationService;

    public CommentService(CommentRepository commentRepository, IUserService userService, IPublicationService publicationService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.publicationService = publicationService;
    }

    @Override
    public CommentEntity addComment(String content, long userId, long publicationId) {

        UserEntity author = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("No users logged in"));

        PublicationEntity publication = publicationService.findById(publicationId)
                .orElseThrow(() -> new IllegalStateException("publication not found"));

        return commentRepository.save(new CommentEntity(content, author, publication));
    }

    @Override
    public void deleteComment(long idComment) {
        if (!commentRepository.existsById(idComment)) {
            throw new IllegalStateException("comment not found");
        }

        commentRepository.deleteById(idComment);
    }
}
