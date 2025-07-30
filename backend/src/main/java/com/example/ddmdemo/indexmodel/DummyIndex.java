package com.example.ddmdemo.indexmodel;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "dummy_index")
@Setting(settingPath = "/configuration/serbian-analyzer-config.json")
public class DummyIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text, store = true, name = "employee_name", analyzer = "serbian_simple")
    private String employeeName;

    @Field(type = FieldType.Text, store = true, name = "organization", analyzer = "serbian_simple")
    private String securityOrganization;

    @Field(type = FieldType.Text, store = true, name = "affected_organization", analyzer = "serbian_simple")
    private String affectedOrganization;

    @Field(type = FieldType.Keyword, store = true, name = "incident_severity")
    private String incidentSeverity; // should be one of: "low", "medium", "high", "critical"

    @Field(type = FieldType.Text, store = true, name = "address", analyzer = "serbian_simple")
    private String affectedOrganizationAddress;

    @Field(type = FieldType.GeoPoint, name = "organizationLocation")
    private GeoPoint organizationLocation;
    
    @Field(type = FieldType.Text, store = true, name = "content_sr", analyzer = "serbian_simple", searchAnalyzer = "serbian_simple")
    private String contentSr;
    
    @Field(type = FieldType.Text, store = true, name = "title")
    private String title;

    @Field(type = FieldType.Text, store = true, name = "server_filename", index = false)
    private String serverFilename;

    @Field(type = FieldType.Integer, store = true, name = "database_id")
    private Integer databaseId;

    @Field(type = FieldType.Dense_Vector, dims = 384, similarity = "cosine")
    private float[] vectorizedContent;
}
