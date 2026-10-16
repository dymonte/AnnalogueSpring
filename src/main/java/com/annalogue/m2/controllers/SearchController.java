package com.annalogue.m2.controllers;

import com.annalogue.m2.dto.SearchResult;
import com.annalogue.m2.services.ISearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final ISearchService searchService;

    public SearchController(ISearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "query", required = false) String query,
            Model model) {

        List<SearchResult> results = List.of();

        if (query != null && !query.isBlank()) {
            results = searchService.search(query);
        }

        model.addAttribute("query", query);
        model.addAttribute("results", results);

        return "search";
    }
}