package com.example.ddmdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParsedTextDTO {
    private String title;
    private String employeeName;
    private String securityOrganization;
    private String affectedOrganization;
    private String incidentSeverity;
    private String affectedOrganizationAddress;
    private String documentId;
}