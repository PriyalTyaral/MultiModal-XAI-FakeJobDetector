package com.example.random_major.model;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JobResultDTO: DTO for displaying job results on dashboard
 * 
 * Contains all relevant information without database internals
 */
public class JobResultDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("inputType")
    private String inputType;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("prediction")
    private String prediction;

    @JsonProperty("confidenceScore")
    private double confidenceScore;

    @JsonProperty("baseModelScore")
    private double baseModelScore;

    @JsonProperty("adjustmentFactor")
    private double adjustmentFactor;

    @JsonProperty("companyVerification")
    private Map<String, Object> companyVerification;

    @JsonProperty("domainValidation")
    private Map<String, Object> domainValidation;

    @JsonProperty("externalValidationInfluence")
    private String externalValidationInfluence;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("isRisky")
    private boolean isRisky; // True if confidence > 0.6 and prediction is FAKE

    @JsonProperty("originalInput")
    private String originalInput; // Preview of the original text

    @JsonProperty("redFlagScore")
    private double redFlagScore; // Score from red flag detection (0-1)

    @JsonProperty("redFlagsDetected")
    private java.util.List<RedFlag> redFlagsDetected; // List of detected red flags

    // ── Constructors ───────────────────────────────────────
    public JobResultDTO() {}

    public JobResultDTO(
        String id,
        String inputType,
        String companyName,
        String prediction,
        double confidenceScore,
        double baseModelScore,
        double adjustmentFactor,
        Map<String, Object> companyVerification,
        Map<String, Object> domainValidation,
        String externalValidationInfluence,
        LocalDateTime createdAt,
        String originalInput
    ) {
        this.id = id;
        this.inputType = inputType;
        this.companyName = companyName;
        this.prediction = prediction;
        this.confidenceScore = confidenceScore;
        this.baseModelScore = baseModelScore;
        this.adjustmentFactor = adjustmentFactor;
        this.companyVerification = companyVerification;
        this.domainValidation = domainValidation;
        this.externalValidationInfluence = externalValidationInfluence;
        this.createdAt = createdAt;
        this.originalInput = originalInput;
        this.isRisky = "FAKE".equals(prediction) && confidenceScore > 0.6;
    }

    // ── Getters ─────────────────────────────────────────
    public String getId() { return id; }
    public String getInputType() { return inputType; }
    public String getCompanyName() { return companyName; }
    public String getPrediction() { return prediction; }
    public double getConfidenceScore() { return confidenceScore; }
    public double getBaseModelScore() { return baseModelScore; }
    public double getAdjustmentFactor() { return adjustmentFactor; }
    public Map<String, Object> getCompanyVerification() { return companyVerification; }
    public Map<String, Object> getDomainValidation() { return domainValidation; }
    public String getExternalValidationInfluence() { return externalValidationInfluence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRisky() { return isRisky; }
    public String getOriginalInput() { return originalInput; }
    public double getRedFlagScore() { return redFlagScore; }
    public java.util.List<RedFlag> getRedFlagsDetected() { return redFlagsDetected; }

    public String getOriginalInputPreview() {
        if (originalInput == null || originalInput.length() <= 100) {
            return originalInput;
        }
        return originalInput.substring(0, 100) + "...";
    }

    // ── Setters ─────────────────────────────────────────
    public void setId(String id) { this.id = id; }
    public void setInputType(String inputType) { this.inputType = inputType; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setPrediction(String prediction) { this.prediction = prediction; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public void setBaseModelScore(double baseModelScore) { this.baseModelScore = baseModelScore; }
    public void setAdjustmentFactor(double adjustmentFactor) { this.adjustmentFactor = adjustmentFactor; }
    public void setCompanyVerification(Map<String, Object> companyVerification) { this.companyVerification = companyVerification; }
    public void setDomainValidation(Map<String, Object> domainValidation) { this.domainValidation = domainValidation; }
    public void setExternalValidationInfluence(String externalValidationInfluence) { this.externalValidationInfluence = externalValidationInfluence; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRisky(boolean risky) { this.isRisky = risky; }
    public void setOriginalInput(String originalInput) { this.originalInput = originalInput; }
    public void setRedFlagScore(double redFlagScore) { this.redFlagScore = redFlagScore; }
    public void setRedFlagsDetected(java.util.List<RedFlag> redFlagsDetected) { this.redFlagsDetected = redFlagsDetected; }
}
