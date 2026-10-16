package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.UploadFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface IFilesService {
    File saveMultipartFile(MultipartFile file) throws UploadFileException;
}
