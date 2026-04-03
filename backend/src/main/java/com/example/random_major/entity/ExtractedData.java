package com.example.random_major.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ExtractedData DTO: Stores extracted structured information from job text
 * 
 * This DTO holds the output of the EntityExtractionService:
 * - Company Name: Extracted from text patterns or domain
 * - URL: First valid URL found in text
 * - Domain: Extracted from URL using java.net.URI
 */
public class ExtractedData {

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("url")
    private String url;

    @JsonProperty("domain")
    private String domain;

    // ── Constructors ───────────────────────────────────────────
    public ExtractedData() {
    }

    public ExtractedData(String companyName, String url, String domain) {
        this.companyName = companyName;
        this.url = url;
        this.domain = domain;
    }

    // ── Getters ─────────────────────────────────────────────────
    public String getCompanyName() {
        return companyName;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain() {
        return domain;
    }

    // ── Setters ─────────────────────────────────────────────────
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    // ── String Representation ───────────────────────────────────
    @Override
    public String toString() {
        return "ExtractedData{" +
                "companyName='" + companyName + '\'' +
                ", url='" + url + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
