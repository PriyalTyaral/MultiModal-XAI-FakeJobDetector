package com.fakejobdetection.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AnalysisResult {

    private String inputText;
    private Features features;
    private Prediction prediction;
    private Explanation explanation;
    private Instant createdAt = Instant.now();

    // ===== GETTERS & SETTERS =====

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public Explanation getExplanation() {
        return explanation;
    }

    public void setExplanation(Explanation explanation) {
        this.explanation = explanation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // ===== INNER CLASSES =====

    public static class Features {
        public boolean salaryAnomaly;
        public double keywordScore;
    }

    public static class Prediction {
        public String label;
        public double confidence;
    }

    public static class Explanation {
        public List<String> reasons;
        public Map<String, Double> weights;
    }
}
