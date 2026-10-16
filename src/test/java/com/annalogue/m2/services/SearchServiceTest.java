package com.annalogue.m2.services;

import com.annalogue.m2.dto.SearchResult;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.PublicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private IUserService userService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchShouldReturnEmptyListWhenQueryIsNull() {
        List<SearchResult> results = searchService.search(null);

        assertEquals(List.of(), results);

        verifyNoInteractions(
                moduleRepository,
                publicationRepository,
                userService
        );
    }

    @Test
    void searchShouldReturnEmptyListWhenQueryIsBlank() {
        List<SearchResult> results = searchService.search("    ");

        assertEquals(List.of(), results);

        verifyNoInteractions(
                moduleRepository,
                publicationRepository,
                userService
        );
    }

    @Test
    void searchShouldReturnModuleResultsForCurrentUser() {
        // Arrange
        String query = "Java";
        Long userId = 10L;

        UserEntity user = mock(UserEntity.class);
        ModuleEntity module = mock(ModuleEntity.class);

        when(user.getId()).thenReturn(userId);
        when(userService.getCurrentUser()).thenReturn(user);

        when(module.getName()).thenReturn("Java");
        when(module.getId()).thenReturn(1L);

        when(moduleRepository
                .findModulesByQueryAndUserId(query, userId))
                .thenReturn(List.of(module));

        when(publicationRepository
                .findPublicationsByQueryAndUserId(query, userId))
                .thenReturn(List.of());

        // Act
        List<SearchResult> results = searchService.search(query);

        // Assert
        assertEquals(1, results.size());
        assertEquals(
                new SearchResult("Java", "Module", "/modules/1"),
                results.get(0)
        );

        verify(userService).getCurrentUser();

        verify(moduleRepository)
                .findModulesByQueryAndUserId(query, userId);

        verify(publicationRepository)
                .findPublicationsByQueryAndUserId(query, userId);
    }

    @Test
    void searchShouldReturnPublicationResultsForCurrentUser() {
        // Arrange
        String query = "Java";
        Long userId = 10L;

        UserEntity user = mock(UserEntity.class);
        ModuleEntity module = mock(ModuleEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        when(user.getId()).thenReturn(userId);
        when(userService.getCurrentUser()).thenReturn(user);

        when(module.getId()).thenReturn(2L);

        when(publication.getName()).thenReturn("Java Introduction");
        when(publication.getModule()).thenReturn(module);

        when(moduleRepository
                .findModulesByQueryAndUserId(query, userId))
                .thenReturn(List.of());

        when(publicationRepository
                .findPublicationsByQueryAndUserId(query, userId))
                .thenReturn(List.of(publication));

        // Act
        List<SearchResult> results = searchService.search(query);

        // Assert
        assertEquals(1, results.size());
        assertEquals(
                new SearchResult(
                        "Java Introduction",
                        "Publication",
                        "/modules/2"
                ),
                results.get(0)
        );

        verify(userService).getCurrentUser();

        verify(moduleRepository)
                .findModulesByQueryAndUserId(query, userId);

        verify(publicationRepository)
                .findPublicationsByQueryAndUserId(query, userId);
    }

    @Test
    void searchShouldCombineModuleAndPublicationResultsForCurrentUser() {
        // Arrange
        String query = "Java";
        Long userId = 10L;

        UserEntity user = mock(UserEntity.class);
        ModuleEntity module = mock(ModuleEntity.class);
        ModuleEntity publicationModule = mock(ModuleEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        when(user.getId()).thenReturn(userId);
        when(userService.getCurrentUser()).thenReturn(user);

        when(module.getName()).thenReturn("Java");
        when(module.getId()).thenReturn(1L);

        when(publicationModule.getId()).thenReturn(2L);

        when(publication.getName()).thenReturn("Java Introduction");
        when(publication.getModule()).thenReturn(publicationModule);

        when(moduleRepository
                .findModulesByQueryAndUserId(query, userId))
                .thenReturn(List.of(module));

        when(publicationRepository
                .findPublicationsByQueryAndUserId(query, userId))
                .thenReturn(List.of(publication));

        // Act
        List<SearchResult> results = searchService.search(query);

        // Assert
        assertEquals(2, results.size());

        assertEquals(
                new SearchResult("Java", "Module", "/modules/1"),
                results.get(0)
        );

        assertEquals(
                new SearchResult(
                        "Java Introduction",
                        "Publication",
                        "/modules/2"
                ),
                results.get(1)
        );

        verify(userService).getCurrentUser();

        verify(moduleRepository)
                .findModulesByQueryAndUserId(query, userId);

        verify(publicationRepository)
                .findPublicationsByQueryAndUserId(query, userId);
    }

    @Test
    void searchShouldTrimQueryBeforeSearching() {
        // Arrange
        String query = "   Java   ";
        Long userId = 10L;

        UserEntity user = mock(UserEntity.class);

        when(user.getId()).thenReturn(userId);
        when(userService.getCurrentUser()).thenReturn(user);

        when(moduleRepository
                .findModulesByQueryAndUserId("Java", userId))
                .thenReturn(List.of());

        when(publicationRepository
                .findPublicationsByQueryAndUserId("Java", userId))
                .thenReturn(List.of());

        // Act
        List<SearchResult> results = searchService.search(query);

        // Assert
        assertEquals(List.of(), results);

        verify(userService).getCurrentUser();

        verify(moduleRepository)
                .findModulesByQueryAndUserId("Java", userId);

        verify(publicationRepository)
                .findPublicationsByQueryAndUserId("Java", userId);
    }

    @Test
    void searchShouldReturnEmptyListWhenRepositoriesFindNothing() {
        // Arrange
        String query = "Unknown";
        Long userId = 10L;

        UserEntity user = mock(UserEntity.class);

        when(user.getId()).thenReturn(userId);
        when(userService.getCurrentUser()).thenReturn(user);

        when(moduleRepository
                .findModulesByQueryAndUserId(query, userId))
                .thenReturn(List.of());

        when(publicationRepository
                .findPublicationsByQueryAndUserId(query, userId))
                .thenReturn(List.of());

        // Act
        List<SearchResult> results = searchService.search(query);

        // Assert
        assertEquals(List.of(), results);

        verify(userService).getCurrentUser();

        verify(moduleRepository)
                .findModulesByQueryAndUserId(query, userId);

        verify(publicationRepository)
                .findPublicationsByQueryAndUserId(query, userId);
    }
}