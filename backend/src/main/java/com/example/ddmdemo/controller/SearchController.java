package com.example.ddmdemo.controller;

import com.example.ddmdemo.dto.GeoSearchDTO;
import com.example.ddmdemo.dto.SearchQueryDTO;
import com.example.ddmdemo.dto.SearchResultDTO;
import com.example.ddmdemo.dto.SemanticSearchDTO;
import com.example.ddmdemo.indexmodel.DummyIndex;
import com.example.ddmdemo.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("")
    public Page<SearchResultDTO> advancedSearch(@RequestBody SearchQueryDTO advancedSearchQuery,
                                           Pageable pageable) {
        return searchService.advancedSearch(advancedSearchQuery.expression(), pageable);
    }

    @PostMapping("/knn")
    public Page<SearchResultDTO> semanticSearch(@RequestBody SemanticSearchDTO request,
                                           Pageable pageable) {
        return searchService.semanticSearch(request.query(), pageable);
    }

    @PostMapping("/geo")
    public Page<SearchResultDTO> geoSearch(@RequestBody GeoSearchDTO request, Pageable pageable) {
    return searchService.geoSearch(request.location(), request.radiusKm(), pageable);
}
}
