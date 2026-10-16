package com.annalogue.m2.services;

import com.annalogue.m2.dto.SearchResult;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.PublicationRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchShouldReturnEmptyListWhenQueryIsNull() {
        List<SearchResult> results = searchService.search(null);

        assertEquals(List.of(), results);

        verifyNoInteractions(moduleRepository, publicationRepository);
    }

    @Test
    void searchShouldReturnEmptyListWhenQueryIsBlank() {
        List<SearchResult> results = searchService.search("   ");

        assertEquals(List.of(), results);

        verifyNoInteractions(moduleRepository, publicationRepository);
    }

    @Test
    void searchShouldReturnModuleResults() {
        String query = "Java";

        ModuleEntity module = mock(ModuleEntity.class);

        when(module.getName()).thenReturn("Java");
        when(module.getId()).thenReturn(1L);

        when(moduleRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of(module));

        when(publicationRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of());

        List<SearchResult> results = searchService.search(query);

        assertEquals(1, results.size());

        assertEquals(
                new SearchResult("Java", "Module", "/modules/1"),
                results.get(0)
        );

        verify(moduleRepository).findAllByNameIgnoreCase("Java");
        verify(publicationRepository).findAllByNameIgnoreCase("Java");
    }

    @Test
    void searchShouldReturnPublicationResults() {
        String query = "Java";

        ModuleEntity module = mock(ModuleEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        when(module.getId()).thenReturn(2L);

        when(publication.getName()).thenReturn("Java Introduction");
        when(publication.getModule()).thenReturn(module);

        when(moduleRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of());

        when(publicationRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of(publication));

        List<SearchResult> results = searchService.search(query);

        assertEquals(1, results.size());

        assertEquals(
                new SearchResult(
                        "Java Introduction",
                        "Publication",
                        "/modules/2"
                ),
                results.get(0)
        );

        verify(moduleRepository).findAllByNameIgnoreCase("Java");
        verify(publicationRepository).findAllByNameIgnoreCase("Java");
    }

    @Test
    void searchShouldCombineModuleAndPublicationResults() {
        String query = "Java";

        ModuleEntity module = mock(ModuleEntity.class);
        ModuleEntity publicationModule = mock(ModuleEntity.class);
        PublicationEntity publication = mock(PublicationEntity.class);

        when(module.getName()).thenReturn("Java");
        when(module.getId()).thenReturn(1L);

        when(publicationModule.getId()).thenReturn(2L);

        when(publication.getName()).thenReturn("Java Introduction");
        when(publication.getModule()).thenReturn(publicationModule);

        when(moduleRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of(module));

        when(publicationRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of(publication));

        List<SearchResult> results = searchService.search(query);

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

        verify(moduleRepository).findAllByNameIgnoreCase("Java");
        verify(publicationRepository).findAllByNameIgnoreCase("Java");
    }

    @Test
    void searchShouldTrimQueryBeforeSearching() {
        String query = "  Java  ";

        when(moduleRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of());

        when(publicationRepository.findAllByNameIgnoreCase("Java"))
                .thenReturn(List.of());

        List<SearchResult> results = searchService.search(query);

        assertEquals(List.of(), results);

        verify(moduleRepository).findAllByNameIgnoreCase("Java");
        verify(publicationRepository).findAllByNameIgnoreCase("Java");
    }

    @Test
    void searchShouldReturnEmptyListWhenRepositoriesFindNothing() {
        String query = "Unknown";

        when(moduleRepository.findAllByNameIgnoreCase("Unknown"))
                .thenReturn(List.of());

        when(publicationRepository.findAllByNameIgnoreCase("Unknown"))
                .thenReturn(List.of());

        List<SearchResult> results = searchService.search(query);

        assertEquals(List.of(), results);
    }
}