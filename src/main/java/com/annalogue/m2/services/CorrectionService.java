package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CorrectionEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.CorrectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

@Service
public class CorrectionService implements ICorrectionService {

    @Autowired
    private IUserService userService;

    @Autowired
    private CorrectionRepository correctionRepository;

    @Autowired
    private IPublicationService publicationService;

    @Autowired
    private IFilesService filesService;

    @Override
    public CorrectionEntity addCorrection(
            long userId,
            long publicationId,
            String title,
            MultipartFile file
    ) throws UploadFileException {

        UserEntity author = userService.findById(userId)
                .orElseThrow(() ->
                        new UploadFileException("error.upload.author.notfound"));

        PublicationEntity publication = publicationService.findById(publicationId)
                .orElseThrow(() ->
                        new UploadFileException("error.upload.publication.notfound"));

        if (title == null || title.isBlank()) {
            throw new UploadFileException("A correction title is required.");
        }

        if (file == null || file.isEmpty()) {
            throw new UploadFileException("A correction file is required.");
        }

        File savedFile = filesService.saveMultipartFile(file);

        CorrectionEntity correction = new CorrectionEntity(
                author,
                publication,
                title.trim(),
                savedFile.toString()
        );

        return correctionRepository.save(correction);
    }

    @Override
    public void deleteCorrection(long idCorrection) {
        if (!correctionRepository.existsById(idCorrection)) {
            throw new IllegalStateException("Correction not found");
        }

        correctionRepository.deleteById(idCorrection);
    }

    @Override
    public Resource loadAsResource(long correctionId)
            throws StorageFileNotFoundException {

        CorrectionEntity correction = correctionRepository.findById(correctionId)
                .orElseThrow(() ->
                        new IllegalStateException("Correction not found"));

        if (correction.getPath() == null || correction.getPath().isBlank()) {
            throw new IllegalStateException("Correction file path is missing");
        }

        Resource resource = new FileSystemResource(correction.getPath());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Correction file is unavailable");
        }

        return resource;
    }

    @Override
    public Optional<CorrectionEntity> findById(long correctionId) {
        return correctionRepository.findById(correctionId);
    }
}