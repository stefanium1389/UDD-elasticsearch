package com.example.ddmdemo.service.impl;

import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.ddmdemo.exceptionhandling.exception.MalformedQueryException;
import com.example.ddmdemo.indexmodel.DummyIndex;
import com.example.ddmdemo.service.interfaces.SearchService;
import com.example.ddmdemo.util.ElasticSearchParser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchTemplate;

    public Page<DummyIndex> searchByVector(float[] queryVector, Pageable pageable) {
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

        var searchQuery = NativeQuery.builder()
            .withKnnQuery(knnQuery)
            .withMaxResults(5)
            .withSearchType(null)
            .build();

        var searchHitsPaged =
            SearchHitSupport.searchPageFor(
                elasticsearchTemplate.search(searchQuery, DummyIndex.class),
                searchQuery.getPageable());

        return (Page<DummyIndex>) SearchHitSupport.unwrapSearchHits(searchHitsPaged);
    }

    public Page<DummyIndex> semanticSearch(String query, Pageable pageable) {
      try {
        return searchByVector(VectorizationUtil.getEmbedding(query));
      } catch (TranslateException e) {
        log.error("Vectorization failed");
        return Page.empty();
      }
    }


    @Override
    public Page<DummyIndex> advancedSearch(String expression, Pageable pageable) {
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

        var searchQueryBuilder = new NativeQueryBuilder()
            .withQuery(parsedQuery)
            .withPageable(pageable);

        return runQuery(searchQueryBuilder.build());
    }

    private Page<DummyIndex> runQuery(NativeQuery searchQuery) {

        var searchHits = elasticsearchTemplate.search(searchQuery, DummyIndex.class,
            IndexCoordinates.of("dummy_index"));

        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());

        return (Page<DummyIndex>) SearchHitSupport.unwrapSearchHits(searchHitsPaged);
    }

    public Page<DummyIndex> geoSearch(String locationText, double radiusInKm, Pageable pageable) {
    GeoPoint center;
    try {
        var center = GeocodingUtil.geocode(locationText); // implement this to call external API
    } catch (Exception e) {
        log.error("Geocoding failed for location: " + locationText, e);
        return Page.empty();
    }

    var geoDistanceQuery = new GeoDistanceQuery.Builder()
        .field("organizationLocation")
        .distance(String.format("%.2fkm", radiusInKm))
        .location(new GeoLocation.Builder()
            .lat(center.getLat())
            .lon(center.getLon())
            .build())
        .build();

    var query = new Query.Builder()
        .geoDistance(geoDistanceQuery)
        .build();

    var searchQuery = NativeQuery.builder()
        .withQuery(query)
        .withPageable(pageable)
        .build();

    var searchHits = elasticsearchTemplate.search(searchQuery, DummyIndex.class);
    var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
    return SearchHitSupport.unwrapSearchHits(searchHitsPaged);
}
    
}
