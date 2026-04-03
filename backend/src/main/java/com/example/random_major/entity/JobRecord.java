package com.example.random_major.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JobRecord: Stores job analysis results with company verification and domain validation
 * 
 * Linked to User via userId for one-to-many relationship
 */
@Document(collection = "job_results")
public class JobRecord {

    @Id
    private String id;

    // ── User Link ──────────────────────────────────────────
    private String userId;

    // ── Input Data ─────────────────────────────────────────
    @JsonProperty("inputType")
    private String inputType; // TEXT, IMAGE, AUDIO, DOCUMENT

    @JsonProperty("originalInput")
    private String originalInput; // The original text (for text input) or filename (for files)

    @JsonProperty("extractedText")
    private String extractedText; // Extracted text after OCR/transcription (for files)

    // ── Prediction Results ─────────────────────────────────
    @JsonProperty("prediction")
    private String prediction; // REAL or FAKE

    @JsonProperty("baseModelScore")
    private double baseModelScore; // Original PMML score (0-1)

    @JsonProperty("confidenceScore")
    private double confidenceScore; // Final adjusted score (0-1)

    @JsonProperty("adjustmentFactor")
    private double adjustmentFactor; // How much the score was adjusted

    // ── Company Verification ───────────────────────────────
    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("companyVerification")
    private Map<String, Object> companyVerification; // Nested: exists, status, website, verificationMethod

    // ── Domain Validation ──────────────────────────────────
    @JsonProperty("domainValidation")
    private Map<String, Object> domainValidation; // Nested: match, riskScore, extractedDomain, companyDomain

    // ── LIME Explanations ──────────────────────────────────
    @JsonProperty("lime_explanations")
    private String limeExplanations; // JSON string array of explanations

    // ── Metadata ───────────────────────────────────────────
    @JsonProperty("externalValidationInfluence")
    private String externalValidationInfluence;

    @JsonProperty("redFlagScore")
    private double redFlagScore; // Score from red flag detection (0-1)

    @JsonProperty("redFlagsDetected")
    private String redFlagsDetected; // JSON string array of detected red flags


    private LocalDateTime createdAt;

    // ── Constructors ───────────────────────────────────────
    public JobRecord() {
        this.createdAt = LocalDateTime.now();
        this.inputType = "TEXT";
    }

    public JobRecord(String originalInput, String prediction, double confidenceScore, String limeExplanations, String userId) {
        this.originalInput = originalInput;
        this.prediction = prediction;
        this.confidenceScore = confidenceScore;
        this.limeExplanations = limeExplanations;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.inputType = "TEXT";
    }

    // ── Getters ─────────────────────────────────────────
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getInputType() { return inputType; }
    public String getOriginalInput() { return originalInput; }
    public String getExtractedText() { return extractedText; }
    public String getPrediction() { return prediction; }
    public double getBaseModelScore() { return baseModelScore; }
    public double getConfidenceScore() { return confidenceScore; }
    public double getAdjustmentFactor() { return adjustmentFactor; }
    public String getCompanyName() { return companyName; }
    public Map<String, Object> getCompanyVerification() { return companyVerification; }
    public Map<String, Object> getDomainValidation() { return domainValidation; }
    public String getLimeExplanations() { return limeExplanations; }
    public String getExternalValidationInfluence() { return externalValidationInfluence; }
    public double getRedFlagScore() { return redFlagScore; }
    public String getRedFlagsDetected() { return redFlagsDetected; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ─────────────────────────────────────────
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setInputType(String inputType) { this.inputType = inputType; }
    public void setOriginalInput(String originalInput) { this.originalInput = originalInput; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public void setPrediction(String prediction) { this.prediction = prediction; }
    public void setBaseModelScore(double baseModelScore) { this.baseModelScore = baseModelScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public void setAdjustmentFactor(double adjustmentFactor) { this.adjustmentFactor = adjustmentFactor; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setCompanyVerification(Map<String, Object> companyVerification) { this.companyVerification = companyVerification; }
    public void setDomainValidation(Map<String, Object> domainValidation) { this.domainValidation = domainValidation; }
    public void setLimeExplanations(String limeExplanations) { this.limeExplanations = limeExplanations; }
    public void setExternalValidationInfluence(String externalValidationInfluence) { this.externalValidationInfluence = externalValidationInfluence; }
    public void setRedFlagScore(double redFlagScore) { this.redFlagScore = redFlagScore; }
    public void setRedFlagsDetected(String redFlagsDetected) { this.redFlagsDetected = redFlagsDetected; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}