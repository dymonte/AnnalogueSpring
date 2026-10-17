package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CommentEntity;
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
    private final IFilesService filesService;

    public CommentService(CommentRepository commentRepository, IUserService userService, IPublicationService publicationService, IFilesService filesService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.publicationService = publicationService;
        this.filesService = filesService;
    }

    @Override
    public CommentEntity addCommentToPublication(String content, long userId, long publicationId, MultipartFile file) throws UploadFileException {

        UserEntity author = userService.findById(userId).orElseThrow(() -> new UploadFileException("error.upload.author.notfound"));

        PublicationEntity publication = publicationService.findById(publicationId).orElseThrow(() -> new UploadFileException("error.upload.publication.notfound"));

        File path = null;
        if (file!=null) {
            path = filesService.saveMultipartFile(file);
        }

        return commentRepository.save(new CommentEntity(content, author, publication, path==null?null:path.toString()));
    }

    @Override
    public CommentEntity addCommentToCorrection(String content, long userId, long correctionId, MultipartFile file) throws UploadFileException {
        return null;
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
