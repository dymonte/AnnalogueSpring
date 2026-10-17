package com.annalogue.m2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CorrectionEntityTest {

    @Test
    void shouldCreateEmptyCorrection() {

        CorrectionEntity correction = new CorrectionEntity();

        assertNotNull(correction);
        assertNull(correction.getId());
        assertNull(correction.getTitle());
        assertNull(correction.getAuthorComment());
        assertNull(correction.getPublication());
        assertNull(correction.getPath());
    }


    @Test
    void shouldCreateCorrectionWithConstructor() {

        UserEntity author = mock(UserEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        CorrectionEntity correction = new CorrectionEntity(
                author,
                publication,
                "Ma correction",
                "/uploads/correction.pdf"
        );

        assertEquals("Ma correction", correction.getTitle());
        assertEquals("/uploads/correction.pdf", correction.getPath());

        assertSame(author, correction.getAuthorComment());
        assertSame(publication, correction.getPublication());

        // L'identifiant est généré par JPA lors de la persistance.
        assertNull(correction.getId());
    }


    @Test
    void shouldGetAndSetTitle() {

        CorrectionEntity correction = new CorrectionEntity();

        correction.setTitle("Nouvelle correction");

        assertEquals("Nouvelle correction", correction.getTitle());
    }


    @Test
    void shouldGetAndSetAuthorComment() {

        CorrectionEntity correction = new CorrectionEntity();
        UserEntity author = mock(UserEntity.class);

        correction.setAuthorComment(author);

        assertSame(author, correction.getAuthorComment());
    }


    @Test
    void shouldGetAndSetPublication() {

        CorrectionEntity correction = new CorrectionEntity();
        PublicationEntity publication = mock(PublicationEntity.class);

        correction.setPublication(publication);

        assertSame(publication, correction.getPublication());
    }


    @Test
    void shouldGetAndSetPath() {

        CorrectionEntity correction = new CorrectionEntity();

        correction.setPath("/uploads/new-correction.pdf");

        assertEquals(
                "/uploads/new-correction.pdf",
                correction.getPath()
        );
    }


    @Test
    void shouldAllowNullPath() {

        CorrectionEntity correction = new CorrectionEntity(
                mock(UserEntity.class),
                mock(PublicationEntity.class),
                "Correction",
                "/uploads/correction.pdf"
        );

        correction.setPath(null);

        assertNull(correction.getPath());
    }


    @Test
    void shouldAllowUpdatingAllMutableFields() {

        CorrectionEntity correction = new CorrectionEntity();

        UserEntity author = mock(UserEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        correction.setTitle("Correction finale");
        correction.setAuthorComment(author);
        correction.setPublication(publication);
        correction.setPath("/uploads/final.pdf");

        assertAll(
                () -> assertEquals(
                        "Correction finale",
                        correction.getTitle()
                ),
                () -> assertSame(
                        author,
                        correction.getAuthorComment()
                ),
                () -> assertSame(
                        publication,
                        correction.getPublication()
                ),
                () -> assertEquals(
                        "/uploads/final.pdf",
                        correction.getPath()
                )
        );
    }
}