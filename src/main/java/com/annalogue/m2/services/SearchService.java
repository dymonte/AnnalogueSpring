package com.annalogue.m2.services;

import com.annalogue.m2.dto.SearchResult;
import com.annalogue.m2.models.UserEntity;
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

    @Autowired
    private IUserService userService;

    private List<SearchResult> searchForModule(String query, Long userId) {
        return moduleRepository.findModulesByQueryAndUserId(query.trim(), userId)
                .stream()
                .map(module -> new SearchResult(
                        module.getName(),
                        "Module",
                        "/modules/" + module.getId()
                ))
                .toList();
    }

    private List<SearchResult> searchForPublication(String query, Long userId) {
        return publicationRepository.findPublicationsByQueryAndUserId(query.trim(), userId)
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

        UserEntity user = userService.getCurrentUser();

        return Stream.concat(
                searchForModule(query, user.getId()).stream(),
                searchForPublication(query, user.getId()).stream()
        ).toList();
    }
}