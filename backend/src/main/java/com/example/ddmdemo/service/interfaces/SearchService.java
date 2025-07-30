package com.example.ddmdemo.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ddmdemo.dto.SearchResultDTO;

@Service
public interface SearchService {

	Page<SearchResultDTO> advancedSearch(String expression, Pageable pageable);
	Page<SearchResultDTO> semanticSearch(String query, Pageable pageable);
	Page<SearchResultDTO> geoSearch(String location, double radiusKm, Pageable pageable);
    
}
