package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@Service
public class FilesService implements IFilesService{

    @Value("${app.upload.dir}")
    private String uploadDir;

    public File saveMultipartFile(MultipartFile file) throws UploadFileException {

        if (file.getSize() > 10 * 1024 * 1024)
            throw new UploadFileException("error.upload.file.size");

        File fullPath = getFullPath(file);
        try {
            file.transferTo(fullPath);
        }
        catch (IOException e) {
            throw new UploadFileException("error.upload.failed");
        }
        return fullPath;
    }

    private @NonNull File getFullPath(MultipartFile file) throws UploadFileException {


        //save the file in the storage
        File uploadDirectory = new File(uploadDir);

        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new UploadFileException("error.upload.cannotcreatedir");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new UploadFileException("error.upload.filename.missing");
        }
        File fullPath = new File(uploadDirectory, originalFilename);

        // avoid overwriting existing files by adding a number
        int i = 1;
        while (fullPath.exists()) {
            fullPath = new File(uploadDirectory, originalFilename.substring(0, originalFilename.lastIndexOf('.')) + "-" + i + originalFilename.substring(originalFilename.lastIndexOf('.')));
            i++;
        }
        return fullPath;
    }

    public Resource loadAsResource(String stringPath) throws StorageFileNotFoundException {
        try {
            if (stringPath == null) {
                throw new StorageFileNotFoundException();
            }
            Path file = Path.of(stringPath);
            Resource resource = new FileSystemResource(file);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException();

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException();
        }
    }

}
