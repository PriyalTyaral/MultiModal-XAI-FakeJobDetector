package com.example.random_major.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "job_results")
public class JobRecord {

    @Id
    private String id;

    private String text;

    private String result;

    // Store confidence as percentage (0–100)
    @JsonProperty("fake_confidence")
    private double fakeConfidence;

    // Store LIME explanation as JSON string
    private String explanation;

    // GCS blob URL for stored explanation (empty if GCP offline)
    @JsonProperty("gcs_blob_url")
    private String gcsUrl;

    private String userId;

    private LocalDateTime createdAt;

    public JobRecord() {
        this.createdAt = LocalDateTime.now();
    }

    public JobRecord(String text, String result, double fakeConfidence, String explanation, String userId) {
        this.text = text;
        this.result = result;
        this.fakeConfidence = fakeConfidence;
        this.explanation = explanation;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters ─────────────────────────────────────────
    public String getId() { return id; }
    public String getText() { return text; }
    public String getResult() { return result; }
    public double getFakeConfidence() { return fakeConfidence; }
    public String getExplanation() { return explanation; }
    public String getGcsUrl() { return gcsUrl; }
    public String getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ─────────────────────────────────────────
    public void setText(String text) { this.text = text; }
    public void setResult(String result) { this.result = result; }
    public void setFakeConfidence(double fakeConfidence) { this.fakeConfidence = fakeConfidence; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setGcsUrl(String gcsUrl) { this.gcsUrl = gcsUrl; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}