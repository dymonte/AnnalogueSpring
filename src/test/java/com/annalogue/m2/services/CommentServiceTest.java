package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IUserService userService;

    @Mock
    private IPublicationService publicationService;

    @Mock
    private IFilesService filesService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void shouldAddComment() throws UploadFileException {

        UserEntity author = mock(UserEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        when(userService.findById(1L)).thenReturn(Optional.of(author));
        when(publicationService.findById(2L)).thenReturn(Optional.of(publication));
        when(commentRepository.save(any(CommentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentEntity result = commentService.addComment("Super !", 1L, 2L, null);

        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());

        CommentEntity saved = captor.getValue();
        assertEquals("Super !", saved.getContent());
        assertEquals(author, saved.getAuthorComment());
        assertEquals(publication, saved.getPublication());
        assertEquals(saved, result);
    }

    @Test
    void shouldThrowWhenUserNotFound() {

        when(userService.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UploadFileException.class,
                () -> commentService.addComment("Super !", 1L, 2L, null));

        verifyNoInteractions(publicationService);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPublicationNotFound() {

        when(userService.findById(1L)).thenReturn(Optional.of(mock(UserEntity.class)));
        when(publicationService.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UploadFileException.class,
                () -> commentService.addComment("Super !", 1L, 2L, null));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteComment() {

        when(commentRepository.existsById(3L)).thenReturn(true);

        commentService.deleteComment(3L);

        verify(commentRepository).deleteById(3L);
    }

    @Test
    void shouldThrowWhenDeletingUnknownComment() {

        when(commentRepository.existsById(3L)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> commentService.deleteComment(3L));

        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldSaveFileWhenAddingCommentWithFile() throws UploadFileException {

        UserEntity author = mock(UserEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);
        File savedFile = mock(File.class);
        when(savedFile.toString()).thenReturn("/uploads/test.txt");

        when(userService.findById(1L)).thenReturn(Optional.of(author));
        when(publicationService.findById(2L)).thenReturn(Optional.of(publication));
        when(filesService.saveMultipartFile(any(MultipartFile.class))).thenReturn(savedFile);
        when(commentRepository.save(any(CommentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

        CommentEntity result = commentService.addComment("Super !", 1L, 2L, file);

        verify(filesService).saveMultipartFile(file);
        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());

        CommentEntity saved = captor.getValue();
        assertEquals("/uploads/test.txt", saved.getPath());
    }

    @Test
    void shouldLoadAsResourceSuccessfully() throws StorageFileNotFoundException {
        CommentEntity comment = mock(CommentEntity.class);
        Resource mockResource = mock(Resource.class);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(comment.getPath()).thenReturn("/uploads/test.txt");
        when(filesService.loadAsResource("/uploads/test.txt")).thenReturn(mockResource);

        Resource result = commentService.loadAsResource(1L);

        assertNotNull(result);
        verify(commentRepository).findById(1L);
        verify(filesService).loadAsResource("/uploads/test.txt");
    }

    @Test
    void shouldThrowExceptionWhenLoadingNonExistentComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StorageFileNotFoundException.class, () -> commentService.loadAsResource(1L));
    }

    @Test
    void shouldReturnNullWhenCommentHasNoFile() throws StorageFileNotFoundException {
        CommentEntity comment = mock(CommentEntity.class);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(comment.getPath()).thenReturn(null);

        Resource result = commentService.loadAsResource(1L);

        assertNull(result);
        verify(commentRepository).findById(1L);
        verify(filesService, never()).loadAsResource(any());
    }
}