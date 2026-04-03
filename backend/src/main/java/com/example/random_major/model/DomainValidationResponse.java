package com.example.random_major.model;

/**
 * Response object for domain validation
 * Contains domain match status and risk score
 */
public class DomainValidationResponse {
    private boolean match;
    private double riskScore; // 0.0 = no match, 0.5 = partial/suspicious, 1.0 = perfect match
    private String extractedDomain;
    private String companyDomain;
    private String message;

    public DomainValidationResponse() {}

    public DomainValidationResponse(boolean match, double riskScore, String extractedDomain, 
                                   String companyDomain, String message) {
        this.match = match;
        this.riskScore = riskScore;
        this.extractedDomain = extractedDomain;
        this.companyDomain = companyDomain;
        this.message = message;
    }

    // Getters and Setters
    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getExtractedDomain() {
        return extractedDomain;
    }

    public void setExtractedDomain(String extractedDomain) {
        this.extractedDomain = extractedDomain;
    }

    public String getCompanyDomain() {
        return companyDomain;
    }

    public void setCompanyDomain(String companyDomain) {
        this.companyDomain = companyDomain;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
