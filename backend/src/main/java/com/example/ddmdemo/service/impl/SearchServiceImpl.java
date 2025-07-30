package com.example.ddmdemo.service.impl;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.GeoDistanceQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import com.example.ddmdemo.dto.SearchResultDTO;
import com.example.ddmdemo.exceptionhandling.exception.MalformedQueryException;
import com.example.ddmdemo.indexmodel.DummyIndex;
import com.example.ddmdemo.service.interfaces.SearchService;
import com.example.ddmdemo.util.ElasticSearchParser;
import com.example.ddmdemo.util.GeocodingUtil;
import com.example.ddmdemo.util.VectorizationUtil;

import ai.djl.translate.TranslateException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchTemplate;

    public Page<SearchResultDTO> searchByVector(float[] queryVector) {
        Float[] floatObjects = new Float[queryVector.length];
        for (int i = 0; i < queryVector.length; i++) {
            floatObjects[i] = queryVector[i];
        }
        List<Float> floatList = Arrays.stream(floatObjects).collect(Collectors.toList());
        
        var knnQuery = new KnnQuery.Builder()
            .field("vectorizedContent")
            .queryVector(floatList)
            .numCandidates(100)
            .k(10)
            .boost(10.0f)
            .build();
        
        Highlight highlight = new Highlight(List.of(
        		new HighlightField("content_sr"), new HighlightField("title")));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, null);

        var searchQuery = NativeQuery.builder()
            .withKnnQuery(knnQuery)
            .withHighlightQuery(highlightQuery)
            .withMaxResults(5)
            .withSearchType(null)
            .build();

        var searchHitsPaged =
            SearchHitSupport.searchPageFor(
                elasticsearchTemplate.search(searchQuery, DummyIndex.class),
                searchQuery.getPageable());
        List<SearchResultDTO> dtoList = searchHitsPaged.getContent().stream().map(hit -> {
            DummyIndex doc = hit.getContent();
            SearchResultDTO dto = new SearchResultDTO();
            dto.setTitle(doc.getTitle());
            dto.setServerFilename(doc.getServerFilename());
            dto.setHighlightFields(hit.getHighlightFields()); // Map<String, List<String>>
            System.out.println(hit.getHighlightFields());
            return dto;
        }).toList();

        return new PageImpl<>(dtoList, searchQuery.getPageable(), searchHitsPaged.getTotalElements());
    }

    public Page<SearchResultDTO> semanticSearch(String query, Pageable pageable) {
      try {
        return searchByVector(VectorizationUtil.getEmbedding(query));
      } catch (TranslateException e) {
        log.error("Vectorization failed");
        return Page.empty();
      }
    }


    @Override
    public Page<SearchResultDTO> advancedSearch(String expression, Pageable pageable) {
        if (expression == null || expression.isEmpty()) {
            throw new MalformedQueryException("Search query is empty.");
        }

        // Use your ElasticSearchParser here:
        ElasticSearchParser parser = new ElasticSearchParser(expression);
        Query parsedQuery;
        try {
            parsedQuery = parser.parse();  // Assuming parse() returns co.elastic.clients.elasticsearch._types.query_dsl.Query
        } catch (Exception e) {
            throw new MalformedQueryException("Failed to parse search expression: " + e.getMessage());
        }
        Highlight highlight = new Highlight(List.of(
        		new HighlightField("content_sr"), new HighlightField("title")));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, null);
        var searchQueryBuilder = new NativeQueryBuilder()
            .withQuery(parsedQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(pageable);

        return runQuery(searchQueryBuilder.build());
    }

    private Page<SearchResultDTO> runQuery(NativeQuery searchQuery) {

        var searchHits = elasticsearchTemplate.search(searchQuery, DummyIndex.class,
            IndexCoordinates.of("dummy_index"));

        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());

        List<SearchResultDTO> dtoList = searchHitsPaged.getContent().stream().map(hit -> {
            DummyIndex doc = hit.getContent();
            SearchResultDTO dto = new SearchResultDTO();
            dto.setTitle(doc.getTitle());
            dto.setServerFilename(doc.getServerFilename());
            dto.setHighlightFields(hit.getHighlightFields());
            System.out.println(hit.getHighlightFields());
            return dto;
        }).toList();
        return new PageImpl<>(dtoList, searchQuery.getPageable(), searchHitsPaged.getTotalElements());
    }

    public Page<SearchResultDTO> geoSearch(String locationText, double radiusInKm, Pageable pageable) {
        GeoPoint center;
        try {
            center = GeocodingUtil.geocode(locationText); // This should return a Spring GeoPoint (lat, lon)
        } catch (Exception e) {
            log.error("Geocoding failed for location: " + locationText, e);
            return Page.empty();
        }

        // Create GeoLocation from GeoPoint
        GeoLocation location = GeoLocation.of(geo -> geo
            .latlon(builder -> builder
                .lat(center.getLat())
                .lon(center.getLon())
            )
        );

        // Build geo_distance query
        GeoDistanceQuery geoDistanceQuery = new GeoDistanceQuery.Builder()
            .field("organizationLocation")
            .distance(String.format("%.2fkm", radiusInKm))
            .location(location)
            .build();

        // Wrap into a Query object
        Query query = new Query.Builder()
            .geoDistance(geoDistanceQuery)
            .build();

        // Build NativeQuery and execute
        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(query)
            .withPageable(pageable)
            .build();

        return runQuery(searchQuery);
    }
    
}
