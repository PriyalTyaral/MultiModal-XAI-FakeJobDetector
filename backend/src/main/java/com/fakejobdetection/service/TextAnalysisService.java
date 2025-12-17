package com.fakejobdetection.service;

import com.fakejobdetection.entity.AnalysisResult;
import com.fakejobdetection.util.OpenNLPProcessor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TextAnalysisService {

    private static final List<String> SUSPICIOUS_KEYWORDS =
            List.of("urgent", "no interview", "limited slots", "pay immediately");

    public AnalysisResult analyze(String text) {

        List<String> tokens = OpenNLPProcessor.tokenize(text.toLowerCase());
        int sentenceCount = OpenNLPProcessor.sentenceCount(text);

        // Keyword frequency
        long keywordMatches = tokens.stream()
                .filter(SUSPICIOUS_KEYWORDS::contains)
                .count();

        boolean suspicious = keywordMatches >= 2 || sentenceCount <= 2;

        double keywordScore = Math.min(1.0, keywordMatches * 0.35);

        String label = suspicious ? "FAKE" : "REAL";
        double confidence = suspicious ? 0.90 : 0.75;

        // Explanation
        List<String> reasons = new ArrayList<>();
        Map<String, Double> weights = new HashMap<>();

        if (keywordMatches > 0) {
            reasons.add("Suspicious keywords detected");
            weights.put("keyword_score", keywordScore);
        }

        if (sentenceCount <= 2) {
            reasons.add("Very short job description");
            weights.put("sentence_length", 0.25);
        }

        if (reasons.isEmpty()) {
            reasons.add("No suspicious linguistic patterns found");
            weights.put("clean_text", 0.10);
        }

        // Build result
        AnalysisResult result = new AnalysisResult();
        result.setInputText(text);

        AnalysisResult.Features features = new AnalysisResult.Features();
        features.salaryAnomaly = suspicious;
        features.keywordScore = keywordScore;

        AnalysisResult.Prediction prediction = new AnalysisResult.Prediction();
        prediction.label = label;
        prediction.confidence = confidence;

        AnalysisResult.Explanation explanation = new AnalysisResult.Explanation();
        explanation.reasons = reasons;
        explanation.weights = weights;

        result.setFeatures(features);
        result.setPrediction(prediction);
        result.setExplanation(explanation);

        return result;
    }
}
