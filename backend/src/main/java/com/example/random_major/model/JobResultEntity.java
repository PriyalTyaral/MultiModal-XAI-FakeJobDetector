package com.example.random_major.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "job_results")
public class JobResultEntity {

    @Id
    private String id; // MongoDB _id

    private String text;              // The job posting text
    private String finalLabel;             // "real" or "fake"
    private double confidenceFake;  // Probability of being fake
    private String explanations;       // LIME explanations as JSON

    public JobResultEntity() {}

    public JobResultEntity(String text, String finalLabel, double confidenceFake, String explanations) {
        this.text = text;
        this.finalLabel = finalLabel;
        this.confidenceFake = confidenceFake;
        this.explanations = explanations;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getfinalLabel() {
        return finalLabel;
    }

    public void setfinalLabel(String finalLabel) {
        this.finalLabel = finalLabel;
    }

    public double getProbability_fake() {
        return confidenceFake;
    }

    public void setProbability_fake(double confidenceFake) {
        this.confidenceFake = confidenceFake;
    }

    public String getexplanations() {
        return explanations;
    }

    public void setexplanations(String explanations) {
        this.explanations = explanations;
    }
}
