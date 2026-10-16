package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class FilesServiceTest {

    @Autowired
    private IFilesService filesService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @AfterEach
    void cleanUp() throws IOException {
        File uploadDirectory = new File(uploadDir);
        if (uploadDirectory.exists()) {
            File[] files = uploadDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            uploadDirectory.delete();
        }
    }

    @Test
    void shouldSaveFileSuccessfully() throws UploadFileException, IOException {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        File result = filesService.saveMultipartFile(file);

        assertTrue(result.exists());
        assertEquals("test.txt", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenFileTooLarge() {
        MultipartFile largeFile = new MockMultipartFile("file", "large.txt", "text/plain", new byte[11 * 1024 * 1024]);

        UploadFileException exception = assertThrows(UploadFileException.class, () -> filesService.saveMultipartFile(largeFile));

        assertEquals("error.upload.file.size", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFilenameIsNull() {
        MultipartFile file = new MockMultipartFile("file", null, "text/plain", "content".getBytes());

        UploadFileException exception = assertThrows(UploadFileException.class, () -> filesService.saveMultipartFile(file));

        assertEquals("error.upload.filename.missing", exception.getMessage());
    }

    @Test
    void shouldHandleFilenameCollision() throws UploadFileException {
        MultipartFile file1 = new MockMultipartFile("file", "test.txt", "text/plain", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "test.txt", "text/plain", "content2".getBytes());

        File result1 = filesService.saveMultipartFile(file1);
        File result2 = filesService.saveMultipartFile(file2);

        assertNotEquals(result1.getName(), result2.getName());
        assertTrue(result2.getName().startsWith("test-"));
    }

    @Test
    void shouldLoadAsResourceSuccessfully() throws UploadFileException, IOException {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        File savedFile = filesService.saveMultipartFile(file);

        Resource resource = filesService.loadAsResource(savedFile.toString());

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void shouldThrowExceptionWhenLoadingNullPath() {
        assertThrows(StorageFileNotFoundException.class, () -> filesService.loadAsResource(null));
    }

    @Test
    void shouldThrowExceptionWhenLoadingNonExistentFile() {
        assertThrows(StorageFileNotFoundException.class, () -> filesService.loadAsResource("./test-uploads/nonexistent.txt"));
    }
}
