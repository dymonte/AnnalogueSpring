package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CorrectionEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.ICorrectionService;
import com.annalogue.m2.services.IPublicationService;
import com.annalogue.m2.services.IUserService;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CorrectionController {

    private final ICorrectionService correctionService;
    private final IUserService userService;
    private final IPublicationService publicationService;

    public CorrectionController(
            ICorrectionService correctionService,
            IUserService userService,
            IPublicationService publicationService) {
        this.correctionService = correctionService;
        this.userService = userService;
        this.publicationService = publicationService;
    }

    @PostMapping("/publication/{id}/addCorrection")
    public String addCorrection(
            @PathVariable("id") long publicationId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            Model model) {

        UserEntity currentUser = userService.getCurrentUser();

        PublicationEntity publication = publicationService.findById(publicationId)
                .orElse(null);

        if (publication == null) {
            return "redirect:/";
        }

        try {
            correctionService.addCorrection(
                    currentUser.getId(),
                    publicationId,
                    title,
                    file
            );
        } catch (UploadFileException e) {
            // A redirect does not preserve model attributes.
            // Log the exception or use a flash attribute if needed.
            return "redirect:/modules/" + publication.getModule().getId();
        }

        return "redirect:/modules/" + publication.getModule().getId();
    }

    @GetMapping("/corrections/{id}/download")
    public ResponseEntity<Resource> downloadCorrection(
            @PathVariable("id") long correctionId)
            throws StorageFileNotFoundException {

        CorrectionEntity correction = correctionService.findById(correctionId)
                .orElse(null);

        if (correction == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = correctionService.loadAsResource(correctionId);

        String filename = correction.getTitle()
                .replace("\"", "")
                .replace("\r", "")
                .replace("\n", "");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(filename)
                                .build()
                                .toString()
                )
                .body(resource);
    }

    @PostMapping("/deleteCorrection/{id}")
    public String deleteCorrection(@PathVariable("id") long correctionId) {

        CorrectionEntity correction = correctionService.findById(correctionId)
                .orElse(null);

        if (correction == null) {
            return "redirect:/";
        }

        Long moduleId = correction.getPublication().getModule().getId();

        correctionService.deleteCorrection(correctionId);

        return "redirect:/modules/" + moduleId;
    }
}