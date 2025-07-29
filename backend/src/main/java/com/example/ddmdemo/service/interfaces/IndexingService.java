package com.example.ddmdemo.service.interfaces;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ddmdemo.dto.ParsedTextDTO;
import com.example.ddmdemo.indexmodel.DummyIndex;

@Service
public interface IndexingService {

    String indexDocument(ParsedTextDTO documentFile) throws Exception;
    ParsedTextDTO parseDocument(MultipartFile documentFile);
	void deleteDocument(String id);
    
}
