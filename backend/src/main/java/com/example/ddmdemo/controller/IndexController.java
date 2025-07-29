package com.example.ddmdemo.controller;

import com.example.ddmdemo.dto.DummyDocumentFileDTO;
import com.example.ddmdemo.dto.DummyDocumentFileResponseDTO;
import com.example.ddmdemo.dto.ParsedTextDTO;
import com.example.ddmdemo.service.interfaces.IndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class IndexController {

    private final IndexingService indexingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParsedTextDTO addDocumentFile(@RequestPart("file") MultipartFile file) {
        var serverFilename = indexingService.parseDocument(file);
        return serverFilename;
    }
    
    @PostMapping("/confirm")
    public DummyDocumentFileResponseDTO confirmIndexedFile(@RequestBody ParsedTextDTO dto) {
    	try {
        	var serverFilename = indexingService.indexDocument(dto);
        	return new DummyDocumentFileResponseDTO(serverFilename);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
		return null;
    }
    
    @DeleteMapping("/decline/{tempId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineIndexedFile(@PathVariable("tempId") String id) {
    	indexingService.deleteDocument(id);
    }
}
