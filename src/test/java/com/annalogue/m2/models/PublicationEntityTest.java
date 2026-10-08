package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PublicationEntityTest {

    @Test
    void shouldCreatePublicationWithConstructor() {

        PublicationEntity publication = new PublicationEntity(
                "FileName",
                "description",
                FileType.CM,
                new ModuleEntity(),
                new UserEntity(),
                "path/to/file.pdf");

        assertEquals("FileName", publication.getName());
        assertEquals("description", publication.getDescription());
        assertEquals(FileType.CM, publication.getFileType());
        assertEquals("path/to/file.pdf", publication.getFilePath());
        assertNull(publication.getId());
    }

    @Test
    void shouldSetAndGetAllFields() {

        PublicationEntity publication = new PublicationEntity();
        publication.setName("FileName");
        publication.setDescription("description");
        publication.setFilePath("path/to/file.pdf");
        publication.setFileType(FileType.CM);
        publication.setModule(new ModuleEntity());
        publication.setAuthor(new UserEntity());

        assertEquals("FileName", publication.getName());
        assertEquals("description", publication.getDescription());
        assertEquals(FileType.CM, publication.getFileType());
        assertEquals("path/to/file.pdf", publication.getFilePath());

    }

}