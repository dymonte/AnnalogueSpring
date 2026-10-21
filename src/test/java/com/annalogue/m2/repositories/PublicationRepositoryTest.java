package com.annalogue.m2.repositories;

import com.annalogue.m2.models.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class PublicationRepositoryTest {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity bfabrice;
    @BeforeEach
    public void setUp(){
        bfabrice= new UserEntity(
                "fabrice.bouquet@ufc",
                "fake-hash",
                "bfabrice",
                Role.PROF,
                null,
                null
        );
        userRepository.save(bfabrice);
    }
    @Test
    void testFindByAuthor() {
        UserEntity author = new UserEntity(
                "test@test.com",
                "pswd",
                "TestName",
                Role.ETUDIANT,
                2000,
                "");

        entityManager.persist(author);

        assertEquals(
                0,
                publicationRepository.findByAuthor(author).size());

        ModuleEntity module = new ModuleEntity(
                "ModuleName", bfabrice);

        entityManager.persist(module);

        PublicationEntity publication = new PublicationEntity(
                "file.pdf",
                "description",
                FileType.CM,
                module,
                author,
                "path/to/file.pdf");

        publicationRepository.save(publication);

        entityManager.flush();

        assertEquals(
                1,
                publicationRepository.findByAuthor(author).size());
    }

    @Test
    void testFindByModule() {
        UserEntity author = new UserEntity(
                "test@test.com",
                "pswd",
                "TestName",
                Role.ETUDIANT,
                2000,
                "");

        entityManager.persist(author);

        assertEquals(
                0,
                publicationRepository.findByAuthor(author).size());

        ModuleEntity module = new ModuleEntity(
                "ModuleName",bfabrice);

        entityManager.persist(module);

        PublicationEntity publication = new PublicationEntity(
                "file.pdf",
                "description",
                FileType.CM,
                module,
                author,
                "path/to/file.pdf");

        publicationRepository.save(publication);

        entityManager.flush();

        assertEquals(
                1,
                publicationRepository.findByAuthor(author).size());
    }
}