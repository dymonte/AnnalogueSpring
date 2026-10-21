package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CorrectionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.ICorrectionService;
import com.annalogue.m2.services.IPublicationService;
import com.annalogue.m2.services.IUserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrectionControllerTest {

    @Mock
    private ICorrectionService correctionService;

    @Mock
    private IUserService userService;

    @Mock
    private IPublicationService publicationService;

    @Mock
    private Model model;

    @InjectMocks
    private CorrectionController correctionController;


    // --------------------------------------------------
    // ADD CORRECTION
    // --------------------------------------------------

    @Test
    void shouldAddCorrectionAndRedirectToModule() throws UploadFileException {

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(7L);

        PublicationEntity publication = mock(PublicationEntity.class);
        when(publication.getModule()).thenReturn(module);

        when(publicationService.findById(2L))
                .thenReturn(Optional.of(publication));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "correction.txt",
                "text/plain",
                "Correction content".getBytes()
        );

        String result = correctionController.addCorrection(
                2L,
                "Ma correction",
                file,
                model
        );

        assertEquals("redirect:/modules/7", result);

        verify(userService).getCurrentUser();
        verify(publicationService).findById(2L);

        verify(correctionService).addCorrection(
                1L,
                2L,
                "Ma correction",
                file
        );
    }


    @Test
    void shouldRedirectToHomeWhenPublicationDoesNotExist()
            throws UploadFileException {

        when(userService.getCurrentUser())
                .thenReturn(mock(UserEntity.class));

        when(publicationService.findById(2L))
                .thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "correction.txt",
                "text/plain",
                "content".getBytes()
        );

        String result = correctionController.addCorrection(
                2L,
                "Ma correction",
                file,
                model
        );

        assertEquals("redirect:/", result);

        verify(correctionService, never())
                .addCorrection(anyLong(), anyLong(), anyString(), any());
    }


    @Test
    void shouldRedirectToModuleWhenUploadFails()
            throws UploadFileException {

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(7L);

        PublicationEntity publication = mock(PublicationEntity.class);
        when(publication.getModule()).thenReturn(module);

        when(publicationService.findById(2L))
                .thenReturn(Optional.of(publication));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "correction.txt",
                "text/plain",
                "content".getBytes()
        );

        when(correctionService.addCorrection(
                1L, 2L, "Ma correction", file
        )).thenThrow(new UploadFileException("Upload failed"));

        String result = correctionController.addCorrection(
                2L,
                "Ma correction",
                file,
                model
        );

        assertEquals("redirect:/modules/7", result);

        verify(correctionService).addCorrection(
                1L,
                2L,
                "Ma correction",
                file
        );
    }


    // --------------------------------------------------
    // DOWNLOAD CORRECTION
    // --------------------------------------------------

    @Test
    void shouldDownloadCorrectionSuccessfully()
            throws StorageFileNotFoundException {

        CorrectionEntity correction = mock(CorrectionEntity.class);
        Resource resource = mock(Resource.class);

        when(correctionService.findById(3L))
                .thenReturn(Optional.of(correction));

        when(correction.getTitle()).thenReturn("Correction.pdf");

        when(correctionService.loadAsResource(3L))
                .thenReturn(resource);

        var response = correctionController.downloadCorrection(3L);

        assertEquals(200, response.getStatusCode().value());

        assertEquals(
                MediaType.APPLICATION_OCTET_STREAM,
                response.getHeaders().getContentType()
        );

        assertSame(resource, response.getBody());

        assertNotNull(
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
        );

        assertTrue(
                response.getHeaders()
                        .getFirst(HttpHeaders.CONTENT_DISPOSITION)
                        .contains("Correction.pdf")
        );

        verify(correctionService).findById(3L);
        verify(correctionService).loadAsResource(3L);
    }


    @Test
    void shouldReturnNotFoundWhenCorrectionDoesNotExist()
            throws StorageFileNotFoundException {

        when(correctionService.findById(3L))
                .thenReturn(Optional.empty());

        var response = correctionController.downloadCorrection(3L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(correctionService, never()).loadAsResource(anyLong());
    }


    @Test
    void shouldSanitizeFilenameWhenDownloadingCorrection()
            throws StorageFileNotFoundException {

        CorrectionEntity correction = mock(CorrectionEntity.class);
        Resource resource = mock(Resource.class);

        when(correctionService.findById(3L))
                .thenReturn(Optional.of(correction));

        when(correction.getTitle())
                .thenReturn("Correction\"\r\n.pdf");

        when(correctionService.loadAsResource(3L))
                .thenReturn(resource);

        var response = correctionController.downloadCorrection(3L);

        assertEquals(200, response.getStatusCode().value());

        String contentDisposition = response.getHeaders()
                .getFirst(HttpHeaders.CONTENT_DISPOSITION);

        assertNotNull(contentDisposition);

        assertFalse(contentDisposition.contains("\r"));
        assertFalse(contentDisposition.contains("\n"));

        assertTrue(contentDisposition.contains("Correction"));
        assertTrue(contentDisposition.contains(".pdf"));

        verify(correctionService).loadAsResource(3L);
    }


    // --------------------------------------------------
    // DELETE CORRECTION
    // --------------------------------------------------

    @Test
    void shouldDeleteCorrectionAndRedirectToModule() {

        ModuleEntity module = mock(ModuleEntity.class);
        when(module.getId()).thenReturn(7L);

        PublicationEntity publication = mock(PublicationEntity.class);
        when(publication.getModule()).thenReturn(module);

        CorrectionEntity correction = mock(CorrectionEntity.class);
        when(correction.getPublication()).thenReturn(publication);

        when(correctionService.findById(3L))
                .thenReturn(Optional.of(correction));

        String result = correctionController.deleteCorrection(3L);

        assertEquals("redirect:/modules/7", result);

        verify(correctionService).findById(3L);
        verify(correctionService).deleteCorrection(3L);
    }


    @Test
    void shouldRedirectToHomeWhenDeletingUnknownCorrection() {

        when(correctionService.findById(3L))
                .thenReturn(Optional.empty());

        String result = correctionController.deleteCorrection(3L);
        assertEquals("redirect:/", result);

        verify(correctionService, never()).deleteCorrection(anyLong());
    }
}