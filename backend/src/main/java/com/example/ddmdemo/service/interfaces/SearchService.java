package com.example.ddmdemo.service.interfaces;

import com.example.ddmdemo.indexmodel.DummyIndex;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface SearchService {

    Page<DummyIndex> advancedSearch(String expression, Pageable pageable);
    public Page<DummyIndex> semanticSearch(String query, Pageable pageable)
    
}
