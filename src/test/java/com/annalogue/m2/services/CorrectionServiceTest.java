package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CorrectionEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.CorrectionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrectionServiceTest {

    @Mock
    private IUserService userService;

    @Mock
    private CorrectionRepository correctionRepository;

    @Mock
    private IPublicationService publicationService;

    @Mock
    private IFilesService filesService;

    @InjectMocks
    private CorrectionService correctionService;


    // --------------------------------------------------
    // ADD CORRECTION
    // --------------------------------------------------

    @Test
    void shouldAddCorrection() throws UploadFileException {

        UserEntity author = mock(UserEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        File savedFile = mock(File.class);
        when(savedFile.toString()).thenReturn("/uploads/test.txt");

        when(userService.findById(1L))
                .thenReturn(Optional.of(author));

        when(publicationService.findById(2L))
                .thenReturn(Optional.of(publication));

        when(filesService.saveMultipartFile(any(MultipartFile.class)))
                .thenReturn(savedFile);

        when(correctionRepository.save(any(CorrectionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Correction content".getBytes()
        );

        CorrectionEntity result = correctionService.addCorrection(
                1L,
                2L,
                " Ma correction ",
                file
        );

        ArgumentCaptor<CorrectionEntity> captor =
                ArgumentCaptor.forClass(CorrectionEntity.class);

        verify(correctionRepository).save(captor.capture());

        CorrectionEntity saved = captor.getValue();

        assertEquals("Ma correction", saved.getTitle());
        assertEquals(author, saved.getAuthorComment());
        assertEquals(publication, saved.getPublication());
        assertEquals("/uploads/test.txt", saved.getPath());

        assertSame(saved, result);

        verify(filesService).saveMultipartFile(file);
        verify(userService).findById(1L);
        verify(publicationService).findById(2L);
    }


    @Test
    void shouldThrowWhenAuthorDoesNotExist() throws UploadFileException {

        when(userService.findById(1L))
                .thenReturn(Optional.empty());

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        assertThrows(
                UploadFileException.class,
                () -> correctionService.addCorrection(
                        1L, 2L, "Correction", file
                )
        );

        verify(publicationService, never()).findById(anyLong());
        verify(filesService, never()).saveMultipartFile(any());
        verify(correctionRepository, never()).save(any());
    }


    @Test
    void shouldThrowWhenPublicationDoesNotExist() throws UploadFileException {

        when(userService.findById(1L))
                .thenReturn(Optional.of(mock(UserEntity.class)));

        when(publicationService.findById(2L))
                .thenReturn(Optional.empty());

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        assertThrows(
                UploadFileException.class,
                () -> correctionService.addCorrection(
                        1L, 2L, "Correction", file
                )
        );

        verify(filesService, never()).saveMultipartFile(any());
        verify(correctionRepository, never()).save(any());
    }


    @Test
    void shouldThrowWhenTitleIsNull() throws UploadFileException {

        prepareValidAuthorAndPublication();

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        assertThrows(
                UploadFileException.class,
                () -> correctionService.addCorrection(
                        1L, 2L, null, file
                )
        );

        verify(filesService, never()).saveMultipartFile(any());
        verify(correctionRepository, never()).save(any());
    }


    @Test
    void shouldThrowWhenTitleIsBlank() throws UploadFileException {

        prepareValidAuthorAndPublication();

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        assertThrows(
                UploadFileException.class,
                () -> correctionService.addCorrection(
                        1L, 2L, "   ", file
                )
        );

        verify(filesService, never()).saveMultipartFile(any());
        verify(correctionRepository, never()).save(any());
    }


    @Test
    void shouldThrowWhenFileIsNull() throws UploadFileException {

        prepareValidAuthorAndPublication();

        assertThrows(
                UploadFileException.class,
                () -> correctionService.addCorrection(
                        1L, 2L, "Correction", null
                )
        );

        verify(filesService, never()).saveMultipartFile(any());
        verify(correctionRepository, never()).save(any());
    }


    @Test
    void shouldThrowWhenFileIsEmpty() throws UploadFileException {

        prepareValidAuthorAndPublication();

        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );

        assertThrows(
                UploadFileException.class,
                () -> correctionService.addCorrection(
                        1L, 2L, "Correction", emptyFile
                )
        );

        verify(filesService, never()).saveMultipartFile(any());
        verify(correctionRepository, never()).save(any());
    }


    // --------------------------------------------------
    // DELETE CORRECTION
    // --------------------------------------------------

    @Test
    void shouldDeleteCorrection() {

        when(correctionRepository.existsById(3L))
                .thenReturn(true);

        correctionService.deleteCorrection(3L);

        verify(correctionRepository).existsById(3L);
        verify(correctionRepository).deleteById(3L);
    }


    @Test
    void shouldThrowWhenDeletingUnknownCorrection() {

        when(correctionRepository.existsById(3L))
                .thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> correctionService.deleteCorrection(3L)
        );

        verify(correctionRepository, never()).deleteById(anyLong());
    }


    // --------------------------------------------------
    // LOAD CORRECTION AS RESOURCE
    // --------------------------------------------------

    @Test
    void shouldLoadAsResourceSuccessfully(@TempDir Path tempDir)
            throws Exception {

        Path filePath = tempDir.resolve("correction.txt");

        Files.writeString(filePath, "Correction content");

        CorrectionEntity correction = mock(CorrectionEntity.class);

        when(correctionRepository.findById(3L))
                .thenReturn(Optional.of(correction));

        when(correction.getPath())
                .thenReturn(filePath.toString());

        Resource result = correctionService.loadAsResource(3L);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isReadable());

        assertEquals(
                "Correction content",
                Files.readString(result.getFile().toPath())
        );

        verify(correctionRepository).findById(3L);
    }


    @Test
    void shouldThrowWhenLoadingUnknownCorrection() {

        when(correctionRepository.findById(3L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> correctionService.loadAsResource(3L)
        );
    }


    @Test
    void shouldThrowWhenCorrectionPathIsNull() {

        CorrectionEntity correction = mock(CorrectionEntity.class);

        when(correctionRepository.findById(3L))
                .thenReturn(Optional.of(correction));

        when(correction.getPath()).thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> correctionService.loadAsResource(3L)
        );
    }


    @Test
    void shouldThrowWhenCorrectionPathIsBlank() {

        CorrectionEntity correction = mock(CorrectionEntity.class);

        when(correctionRepository.findById(3L))
                .thenReturn(Optional.of(correction));

        when(correction.getPath()).thenReturn("   ");

        assertThrows(
                IllegalStateException.class,
                () -> correctionService.loadAsResource(3L)
        );
    }


    @Test
    void shouldThrowWhenCorrectionFileDoesNotExist(@TempDir Path tempDir) {

        CorrectionEntity correction = mock(CorrectionEntity.class);

        when(correctionRepository.findById(3L))
                .thenReturn(Optional.of(correction));

        when(correction.getPath())
                .thenReturn(tempDir.resolve("missing.txt").toString());

        assertThrows(
                IllegalStateException.class,
                () -> correctionService.loadAsResource(3L)
        );
    }


    // --------------------------------------------------
    // FIND CORRECTION
    // --------------------------------------------------

    @Test
    void shouldFindCorrectionById() {

        CorrectionEntity correction = mock(CorrectionEntity.class);

        when(correctionRepository.findById(3L))
                .thenReturn(Optional.of(correction));

        Optional<CorrectionEntity> result =
                correctionService.findById(3L);

        assertTrue(result.isPresent());
        assertSame(correction, result.get());

        verify(correctionRepository).findById(3L);
    }


    @Test
    void shouldReturnEmptyWhenCorrectionDoesNotExist() {

        when(correctionRepository.findById(3L))
                .thenReturn(Optional.empty());

        Optional<CorrectionEntity> result =
                correctionService.findById(3L);

        assertTrue(result.isEmpty());

        verify(correctionRepository).findById(3L);
    }


    // --------------------------------------------------
    // HELPER
    // --------------------------------------------------

    private void prepareValidAuthorAndPublication() {

        when(userService.findById(1L))
                .thenReturn(Optional.of(mock(UserEntity.class)));

        when(publicationService.findById(2L))
                .thenReturn(Optional.of(mock(PublicationEntity.class)));
    }
}