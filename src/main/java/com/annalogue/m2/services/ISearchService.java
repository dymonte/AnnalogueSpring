package com.annalogue.m2.services;

import com.annalogue.m2.dto.SearchResult;

import java.util.List;

public interface ISearchService {
    public List<SearchResult> search(String query);
}
