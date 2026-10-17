package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.CorrectionEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.CommentRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final IUserService userService;
    private final IPublicationService publicationService;
    private final ICorrectionService correctionService;
    private final IFilesService filesService;

    public CommentService(CommentRepository commentRepository, IUserService userService, IPublicationService publicationService, ICorrectionService correctionService, IFilesService filesService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.publicationService = publicationService;
        this.correctionService = correctionService;
        this.filesService = filesService;
    }

    @Override
    public CommentEntity addCommentToPublication(String content, long userId, long publicationId, MultipartFile file) throws UploadFileException {

        UserEntity author = userService.findById(userId).orElseThrow(() -> new UploadFileException("error.upload.author.notfound"));

        PublicationEntity publication = publicationService.findById(publicationId).orElseThrow(() -> new UploadFileException("error.upload.publication.notfound"));

        String path = null;
        if (file != null && !file.isEmpty()) {
            path = filesService.saveMultipartFile(file).toString();
        }

        return commentRepository.save(new CommentEntity(content, author, publication, path));
    }

    @Override
    public CommentEntity addCommentToCorrection(String content, long userId, long correctionId, MultipartFile file) throws UploadFileException {

        UserEntity author = userService.findById(userId)
                .orElseThrow(() -> new UploadFileException("error.upload.author.notfound"));

        CorrectionEntity correction = correctionService.findById(correctionId)
                .orElseThrow(() -> new UploadFileException("error.upload.correction.notfound"));

        String path = null;
        if (file != null && !file.isEmpty()) {
            path = filesService.saveMultipartFile(file).toString();
        }

        return commentRepository.save(new CommentEntity(content, author, correction, path));
    }

    @Override
    public void deleteComment(long idComment) {
        if (!commentRepository.existsById(idComment)) {
            throw new IllegalStateException("comment not found");
        }

        commentRepository.deleteById(idComment);
    }

    @Override
    public Resource loadAsResource(long idComment) throws StorageFileNotFoundException {
        CommentEntity comment = commentRepository.findById(idComment).orElseThrow(() -> new StorageFileNotFoundException());
        String stringPath = comment.getPath();
        if (stringPath == null) {
            return null;
        }
        return filesService.loadAsResource(stringPath);
    }
}
