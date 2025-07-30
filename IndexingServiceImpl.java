package com.example.ddmdemo.service.impl;

import ai.djl.translate.TranslateException;

import com.example.ddmdemo.dto.ParsedTextDTO;
import com.example.ddmdemo.exceptionhandling.exception.LoadingException;
import com.example.ddmdemo.exceptionhandling.exception.StorageException;
import com.example.ddmdemo.indexmodel.DummyIndex;
import com.example.ddmdemo.indexrepository.DummyIndexRepository;
import com.example.ddmdemo.model.DummyTable;
import com.example.ddmdemo.respository.DummyRepository;
import com.example.ddmdemo.service.interfaces.FileService;
import com.example.ddmdemo.service.interfaces.IndexingService;
import com.example.ddmdemo.util.VectorizationUtil;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.language.detect.LanguageDetector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    private final DummyIndexRepository dummyIndexRepository;

    private final DummyRepository dummyRepository;

    private final FileService fileService;

    private final LanguageDetector languageDetector;

    private final Map<String, DummyIndex> tempParsedDocuments = new ConcurrentHashMap<>();
        
    @Override
    @Transactional
    public String indexDocument(ParsedTextDTO dto) throws Exception {
        var newEntity = new DummyTable();
        var index = tempParsedDocuments.get(dto.getDocumentId());
        InputStream stream = fileService.loadAsResource(index.getServerFilename());
        byte[] content = stream.readAllBytes();
        
        MultipartFile documentFile = new MockMultipartFile(
        	    "file",
        	    index.getTitle()+".pdf",
        	    "application/pdf",           
        	    content
        	);

        newEntity.setTitle(index.getTitle());

        var serverFilename = dto.getDocumentId()+".pdf";
        newEntity.setServerFilename(serverFilename);
        index.setServerFilename(serverFilename);
        
        newEntity.setMimeType(detectMimeType(documentFile));
        var savedEntity = dummyRepository.save(newEntity);
        
        try {
            index.setVectorizedContent(VectorizationUtil.getEmbedding(extractDocumentContent(documentFile)));
        } catch (TranslateException e) {
            log.error("Could not calculate vector representation for document with ID: {}",
                savedEntity.getId());
        }
        
        index.setDatabaseId(savedEntity.getId());
        index.setAffectedOrganization(dto.getAffectedOrganization());
        index.setAffectedOrganizationAddress(dto.getAffectedOrganizationAddress());
        index.setOrganizationLocation(GeocodingUtil.geocode(dto.getAffectedOrganizationAddress()));
        index.setEmployeeName(dto.getEmployeeName());
        index.setSecurityOrganization(dto.getSecurityOrganization());
        index.setIncidentSeverity(dto.getIncidentSeverity());
        
        dummyIndexRepository.save(index);

        return serverFilename;
    }
    
    @Override
    @Transactional
    public ParsedTextDTO parseDocument(MultipartFile documentFile) {
        var parsed = new DummyIndex();

        var title = Objects.requireNonNull(documentFile.getOriginalFilename()).split("\\.")[0];
        parsed.setTitle(title);

        var content = extractDocumentContent(documentFile);

        if (detectLanguage(content).equals("SR")) {
            parsed.setContentSr(content);
        }

        Map<String, String> fields = parseFieldsFromContent(content);

        parsed.setEmployeeName(fields.get("employee_name"));
        parsed.setSecurityOrganization(fields.get("organization"));
        parsed.setAffectedOrganization(fields.get("affected_organization"));
        parsed.setIncidentSeverity(fields.get("incident_severity"));
        parsed.setAffectedOrganizationAddress(fields.get("address"));
        
        String docId = UUID.randomUUID().toString();
        var serverFilename = this.fileService.store(documentFile, docId);
        parsed.setServerFilename(serverFilename);
        tempParsedDocuments.put(docId, parsed);

        var dto = new ParsedTextDTO();
        dto.setAffectedOrganization(parsed.getAffectedOrganization());
        dto.setAffectedOrganizationAddress(parsed.getAffectedOrganizationAddress());
        dto.setEmployeeName(parsed.getEmployeeName());
        dto.setSecurityOrganization(parsed.getSecurityOrganization());
        dto.setIncidentSeverity(parsed.getIncidentSeverity());
        dto.setDocumentId(docId);
        dto.setTitle(title);

        return dto;
    }

    private String extractDocumentContent(MultipartFile multipartPdfFile) {
        String documentContent;
        try (var pdfFile = multipartPdfFile.getInputStream()) {
            var pdDocument = PDDocument.load(pdfFile);
            var textStripper = new PDFTextStripper();
            documentContent = textStripper.getText(pdDocument);
            pdDocument.close();
        } catch (IOException e) {
            throw new LoadingException("Error while trying to load PDF file content.");
        }

        return documentContent;
    }

    private String detectLanguage(String text) {
        var detectedLanguage = languageDetector.detect(text).getLanguage().toUpperCase();
        if (detectedLanguage.equals("HR")) {
            detectedLanguage = "SR";
        }

        return detectedLanguage;
    }

    private String detectMimeType(MultipartFile file) {
        var contentAnalyzer = new Tika();

        String trueMimeType;
        String specifiedMimeType;
        try {
            trueMimeType = contentAnalyzer.detect(file.getBytes());
            specifiedMimeType =
                Files.probeContentType(Path.of(Objects.requireNonNull(file.getOriginalFilename())));
        } catch (IOException e) {
            throw new StorageException("Failed to detect mime type for file.");
        }

        if (!trueMimeType.equals(specifiedMimeType) &&
            !(trueMimeType.contains("zip") && specifiedMimeType.contains("zip"))) {
            throw new StorageException("True mime type is different from specified one, aborting.");
        }

        return trueMimeType;
    }
    
    private Map<String, String> parseFieldsFromContent(String content) {
        Map<String, String> fields = new HashMap<>();

        // Example simple parsing logic, adjust regex and field names to your real content
        Pattern employeePattern = Pattern.compile("Naziv Zaposlenog:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = employeePattern.matcher(content);
        if (m.find()) {
            fields.put("employee_name", m.group(1).trim());
        }

        Pattern orgPattern = Pattern.compile("Bezbednosna Organizacija:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        m = orgPattern.matcher(content);
        if (m.find()) {
            fields.put("organization", m.group(1).trim());
        }

        Pattern affectedOrgPattern = Pattern.compile("Pogodjena Organizacija:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        m = affectedOrgPattern.matcher(content);
        if (m.find()) {
            fields.put("affected_organization", m.group(1).trim());
        }

        Pattern severityPattern = Pattern.compile("Ozbiljnost Incidenta:\\s*(niska|srednja|visoka|kriticna)", Pattern.CASE_INSENSITIVE);
        m = severityPattern.matcher(content);
        if (m.find()) {
            fields.put("incident_severity", m.group(1).toLowerCase());
        }

        Pattern addressPattern = Pattern.compile("Adresa Pogodjene Organizacije:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        m = addressPattern.matcher(content);
        if (m.find()) {
            fields.put("address", m.group(1).trim());
        }

        return fields;
    }

	@Override
	public void deleteDocument(String id) {
		tempParsedDocuments.remove(id);
		this.fileService.delete(id+".pdf");
		
	}
}