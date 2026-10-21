package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.CorrectionEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ICorrectionService {
    CorrectionEntity addCorrection(
            long userId,
            long publicationId,
            String title,
            MultipartFile file
    ) throws UploadFileException;

    void deleteCorrection(long idComment);

    Resource loadAsResource(long idComment) throws StorageFileNotFoundException;

    Optional<CorrectionEntity> findById(long correctionId);
}
