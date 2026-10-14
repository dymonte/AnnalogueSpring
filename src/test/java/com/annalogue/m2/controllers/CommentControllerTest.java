package com.annalogue.m2.controllers;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.ICommentService;
import com.annalogue.m2.services.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private ICommentService commentService;

    @Mock
    private IUserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private CommentController commentController;

    @Test
    void shouldAddCommentWithCurrentUserAndRedirectToModuleDetail() {

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(7L);

        PublicationEntity publication = mock(PublicationEntity.class);
        when(publication.getModule()).thenReturn(module);

        CommentEntity comment = mock(CommentEntity.class);
        when(comment.getPublication()).thenReturn(publication);

        when(commentService.addComment("Great", 1L, 2L)).thenReturn(comment);

        String view = commentController.addComment(2L, "Great", model);

        assertEquals("redirect:/modules/detail/7", view);
        verify(commentService).addComment("Great", 1L, 2L);
    }

    @Test
    void shouldDeleteCommentAndRedirect() {

        String view = commentController.deleteStudent(3L);

        assertEquals("redirect:/", view);
        verify(commentService).deleteComment(3L);
    }
}