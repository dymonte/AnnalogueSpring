package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CommentEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ICommentService {
    public CommentEntity addCommentToPublication(String content, long userId, long publicationId, MultipartFile file) throws UploadFileException;

    CommentEntity addCommentToCorrection(String content, long userId, long correctionId, MultipartFile file) throws UploadFileException;

    public void deleteComment(long idComment);
    public Resource loadAsResource(long idComment) throws StorageFileNotFoundException;
}
