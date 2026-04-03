package com.example.random_major.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object for company verification
 * Contains company existence status and details
 */
public class CompanyVerificationResponse {
    private boolean exists;
    private String status; // ACTIVE, INACTIVE, UNKNOWN
    private String website;
    private String message;
    private long apiCallTimeMs;
    
    @JsonProperty("verificationMethod")
    private String verificationMethod; // OPENCORPORATE_API, DNS_FALLBACK, ERROR, NONE

    public CompanyVerificationResponse() {}

    public CompanyVerificationResponse(boolean exists, String status, String website, String message) {
        this.exists = exists;
        this.status = status;
        this.website = website;
        this.message = message;
        this.apiCallTimeMs = 0;
        this.verificationMethod = "UNKNOWN";
    }

    // Getters and Setters
    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getApiCallTimeMs() {
        return apiCallTimeMs;
    }

    public void setApiCallTimeMs(long apiCallTimeMs) {
        this.apiCallTimeMs = apiCallTimeMs;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }
}
