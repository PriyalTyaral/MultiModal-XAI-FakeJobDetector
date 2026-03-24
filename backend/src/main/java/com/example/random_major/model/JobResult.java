package com.example.random_major.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JobResult {

    private String label;

    @JsonProperty("probability_fake")
    private double probabilityFake;

    /** Legacy plain-text explanation field (kept for backward compatibility). */
    private String explanation;

    /** Structured LIME feature-weight pairs (populated after LIME call). */
    @JsonProperty("lime_explanations")
    private List<LimeExplanation> limeExplanations;

    /** Whether the LIME result came from cache: HIT | MISS | ERROR | SKIPPED */
    @JsonProperty("cache_status")
    private String cacheStatus;

    /** Time taken to generate the LIME explanation in milliseconds. */
    @JsonProperty("explanation_latency_ms")
    private long explanationLatencyMs;

    /** GCS blob URL where the explanation JSON was stored (empty if GCP offline). */
    @JsonProperty("gcs_url")
    private String gcsUrl;

    /** The actual text that was analyzed (useful for audio/file flows to show or re-fetch). */
    @JsonProperty("jobText")
    private String jobText;

    public JobResult() {}

    public JobResult(String label, double probabilityFake, String explanation) {
        this.label = label;
        this.probabilityFake = probabilityFake;
        this.explanation = explanation;
    }

    // ── Getters ──────────────────────────────────────────────────
    public String getLabel() { return label; }
    public double getProbabilityFake() { return probabilityFake; }
    public String getExplanation() { return explanation; }
    public List<LimeExplanation> getLimeExplanations() { return limeExplanations; }
    public String getCacheStatus() { return cacheStatus; }
    public long getExplanationLatencyMs() { return explanationLatencyMs; }
    public String getGcsUrl() { return gcsUrl; }
    public String getJobText() { return jobText; }

    // ── Setters ──────────────────────────────────────────────────
    public void setLabel(String label) { this.label = label; }
    public void setProbabilityFake(double probabilityFake) { this.probabilityFake = probabilityFake; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setLimeExplanations(List<LimeExplanation> limeExplanations) { this.limeExplanations = limeExplanations; }
    public void setCacheStatus(String cacheStatus) { this.cacheStatus = cacheStatus; }
    public void setExplanationLatencyMs(long explanationLatencyMs) { this.explanationLatencyMs = explanationLatencyMs; }
    public void setGcsUrl(String gcsUrl) { this.gcsUrl = gcsUrl; }
    public void setJobText(String jobText) { this.jobText = jobText; }
}