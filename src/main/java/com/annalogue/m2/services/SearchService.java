package com.annalogue.m2.services;

import com.annalogue.m2.dto.SearchResult;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class SearchService implements ISearchService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    private List<SearchResult> searchForModule(String query) {
        return moduleRepository.findAllByNameIgnoreCase(query.trim())
                .stream()
                .map(module -> new SearchResult(
                        module.getName(),
                        "Module",
                        "/modules/" + module.getId()
                ))
                .toList();
    }

    private List<SearchResult> searchForPublication(String query) {
        return publicationRepository.findAllByNameIgnoreCase(query.trim())
                .stream()
                .map(publication -> new SearchResult(
                        publication.getName(),
                        "Publication",
                        "/modules/" + publication.getModule().getId()
                ))
                .toList();
    }

    @Override
    public List<SearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return Stream.concat(searchForModule(query).stream(), searchForPublication(query).stream()).toList();
    }
}
