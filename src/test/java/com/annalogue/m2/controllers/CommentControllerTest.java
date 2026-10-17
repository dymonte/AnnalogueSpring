package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.ICommentService;
import com.annalogue.m2.services.IUserService;
import com.annalogue.m2.services.IPublicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private ICommentService commentService;

    @Mock
    private IUserService userService;

    @Mock
    private IPublicationService publicationService;

    @Mock
    private Model model;

    @InjectMocks
    private CommentController commentController;

    @Test
    void shouldAddCommentWithCurrentUserAndRedirectToModuleDetail() throws UploadFileException {

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(7L);

        PublicationEntity publication = mock(PublicationEntity.class);
        when(publication.getModule()).thenReturn(module);

        when(publicationService.findById(2L)).thenReturn(Optional.of(publication));

        String view = commentController.addComment(2L, "Great", null,  model);

        assertEquals("redirect:/modules/detail/7", view);
        verify(commentService).addCommentToPublication("Great", 1L, 2L, null);
    }

    @Test
    void shouldDeleteCommentAndRedirect() {

        String view = commentController.deleteStudent(3L);

        assertEquals("redirect:/", view);
        verify(commentService).deleteComment(3L);
    }

    @Test
    void shouldAddCommentWithFileAndRedirectToModuleDetail() throws UploadFileException {

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(7L);

        PublicationEntity publication = mock(PublicationEntity.class);
        when(publication.getModule()).thenReturn(module);

        when(publicationService.findById(2L)).thenReturn(Optional.of(publication));

        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

        String view = commentController.addComment(2L, "Great with file", file, model);

        assertEquals("redirect:/modules/detail/7", view);
        verify(commentService).addCommentToPublication("Great with file", 1L, 2L, file);
    }
}