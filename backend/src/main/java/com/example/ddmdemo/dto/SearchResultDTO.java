package com.example.ddmdemo.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchResultDTO {
	private String title;
	private String serverFilename;
	private Map<String, List<String>> highlightFields;
}
