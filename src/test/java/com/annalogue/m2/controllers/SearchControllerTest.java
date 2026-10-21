package com.annalogue.m2.controllers;

import com.annalogue.m2.dto.SearchResult;
import com.annalogue.m2.services.ISearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchControllerTest {

    @Mock
    private ISearchService searchService;

    @Mock
    private Model model;

    @InjectMocks
    private SearchController searchController;

    @Test
    void searchShouldReturnSearchViewAndResultsWhenQueryIsProvided() {
        String query = "Java";

        List<SearchResult> expectedResults = List.of(
                new SearchResult("Java module", "Module", "/modules/1"),
                new SearchResult("Java publication", "Publication", "/modules/2")
        );

        when(searchService.search(query)).thenReturn(expectedResults);

        String viewName = searchController.search(query, model);

        assertEquals("search", viewName);
        verify(searchService).search(query);
        verify(model).addAttribute("query", query);
        verify(model).addAttribute("results", expectedResults);
    }

    @Test
    void searchShouldReturnEmptyResultsWhenQueryIsNull() {
        String query = null;

        String viewName = searchController.search(query, model);

        assertEquals("search", viewName);

        verifyNoInteractions(searchService);
        verify(model).addAttribute("query", null);
        verify(model).addAttribute("results", List.of());
    }

    @Test
    void searchShouldReturnEmptyResultsWhenQueryIsEmpty() {
        String query = "";

        String viewName = searchController.search(query, model);

        assertEquals("search", viewName);

        verifyNoInteractions(searchService);
        verify(model).addAttribute("query", "");
        verify(model).addAttribute("results", List.of());
    }

    @Test
    void searchShouldReturnEmptyResultsWhenQueryContainsOnlyWhitespace() {
        String query = "   ";
        String viewName = searchController.search(query, model);

        assertEquals("search", viewName);

        verifyNoInteractions(searchService);
        verify(model).addAttribute("query", query);
        verify(model).addAttribute("results", List.of());
    }

    @Test
    void searchShouldReturnEmptyResultsWhenServiceFindsNothing() {
        String query = "Unknown";
        List<SearchResult> expectedResults = List.of();

        when(searchService.search(query)).thenReturn(expectedResults);

        String viewName = searchController.search(query, model);

        assertEquals("search", viewName);

        verify(searchService).search(query);
        verify(model).addAttribute("query", query);
        verify(model).addAttribute("results", expectedResults);
    }
}
