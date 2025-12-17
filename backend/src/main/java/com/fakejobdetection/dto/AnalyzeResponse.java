package com.fakejobdetection.dto;

import java.util.List;
import java.util.Map;

public class AnalyzeResponse {

    @SuppressWarnings("FieldMayBeFinal")
    private String result;
    @SuppressWarnings("FieldMayBeFinal")
    private double confidence;
    @SuppressWarnings("FieldMayBeFinal")
    private List<String> explanation;
    @SuppressWarnings("FieldMayBeFinal")
    private Map<String, Double> weights;

    public AnalyzeResponse(String result, double confidence,
                           List<String> explanation,
                           Map<String, Double> weights) {
        this.result = result;
        this.confidence = confidence;
        this.explanation = explanation;
        this.weights = weights;
    }

    public String getResult() {
        return result;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<String> getExplanation() {
        return explanation;
    }

    public Map<String, Double> getWeights() {
        return weights;
    }
}
