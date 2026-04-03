package com.example.random_major.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RedFlag: Represents a single red flag detected in a job posting
 */
public class RedFlag {

    @JsonProperty("type")
    private String type; // PAYMENT_REQUEST, TELEGRAM, HIGH_SALARY, URGENCY

    @JsonProperty("weight")
    private double weight; // The weight assigned to this flag (0-1)

    @JsonProperty("description")
    private String description; // Human-readable description

    @JsonProperty("evidence")
    private String evidence; // What was found that triggered this flag

    // ── Constructors ───────────────────────────────────────
    public RedFlag() {}

    public RedFlag(String type, double weight, String description, String evidence) {
        this.type = type;
        this.weight = weight;
        this.description = description;
        this.evidence = evidence;
    }

    // ── Getters ─────────────────────────────────────────
    public String getType() { return type; }
    public double getWeight() { return weight; }
    public String getDescription() { return description; }
    public String getEvidence() { return evidence; }

    // ── Setters ─────────────────────────────────────────
    public void setType(String type) { this.type = type; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setDescription(String description) { this.description = description; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    @Override
    public String toString() {
        return String.format("[%s | %.1f%%] %s", type, weight * 100, description);
    }
}
