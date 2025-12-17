package com.fakejobdetection.service;

import com.fakejobdetection.entity.AnalysisResult;
import com.fakejobdetection.ml.PredictionEngine;
import com.fakejobdetection.util.OpenNLPProcessor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TextAnalysisService {

    public AnalysisResult analyze(String text) {

        // ===============================
        // 1. NLP FEATURE EXTRACTION
        // ===============================

        List<String> tokens = OpenNLPProcessor.tokenize(text.toLowerCase());
        int sentenceCount = OpenNLPProcessor.sentenceCount(text);

        double keywordScore = tokens.stream()
                .filter(t -> t.equals("urgent") || t.equals("interview"))
                .count() * 0.3;

        int urgentFlag = tokens.contains("urgent") ? 1 : 0;
        int noInterviewFlag = text.toLowerCase().contains("no interview") ? 1 : 0;

        Map<String, Object> features = new LinkedHashMap<>();
        features.put("keyword_score", keywordScore);
        features.put("sentence_count", sentenceCount);
        features.put("text_length", text.length());
        features.put("urgent_flag", urgentFlag);
        features.put("no_interview_flag", noInterviewFlag);

        // ===============================
        // 2. ML PREDICTION (PMML)
        // ===============================

        double fakeProbability =
                PredictionEngine.predictProbability(features);

        String label = fakeProbability >= 0.5 ? "FAKE" : "REAL";

        // ===============================
        // 3. TRUE LIME EXPLANATION
        // ===============================

        Map<String, Double> limeWeights =
                getLimeExplanation(features, fakeProbability);

        // ===============================
        // 4. BUILD RESPONSE OBJECT
        // ===============================

        // 🔹 CREATE RESULT OBJECT (MISSING BEFORE)
        AnalysisResult result = new AnalysisResult();
        result.setInputText(text);

        // 🔹 FEATURES
        AnalysisResult.Features f = new AnalysisResult.Features();
        f.salaryAnomaly = fakeProbability >= 0.5;
        f.keywordScore = keywordScore;

        // 🔹 PREDICTION
        AnalysisResult.Prediction p = new AnalysisResult.Prediction();
        p.label = label;
        p.confidence = fakeProbability;

        // 🔹 CLEAN + SET EXPLANATION
        AnalysisResult.Explanation e = new AnalysisResult.Explanation();

        Map<String, Double> cleanedWeights = new LinkedHashMap<>();
        List<String> cleanedReasons = new ArrayList<>();

        for (Map.Entry<String, Double> entry : limeWeights.entrySet()) {
            String cleanKey = normalizeLimeKey(entry.getKey());
            cleanedWeights.put(cleanKey, entry.getValue());
            cleanedReasons.add(cleanKey);
        }

        e.reasons = cleanedReasons;
        e.weights = cleanedWeights;

        // 🔹 ATTACH EVERYTHING
        result.setFeatures(f);
        result.setPrediction(p);
        result.setExplanation(e);

        return result;
    }


     private String normalizeLimeKey(String key) {
        if (key.contains("urgent_flag")) {
            return "Urgent language detected";
        }
        if (key.contains("no_interview_flag")) {
            return "No interview mentioned";
        }
        if (key.contains("keyword_score")) {
            return "Suspicious keywords detected";
        }
        if (key.contains("sentence_count")) {
            return "Unusually short description";
        }
        if (key.contains("text_length")) {
            return "Very short job description";
        }
        return key;
    }

    // =================================================
    // LIME MICROSERVICE CALL (PYTHON)
    // =================================================

    private Map<String, Double> getLimeExplanation(
            Map<String, Object> features,
            double fakeProbability) {

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("features", new Object[]{
                features.get("keyword_score"),
                features.get("sentence_count"),
                features.get("text_length"),
                features.get("urgent_flag"),
                features.get("no_interview_flag")
        });
        request.put("fake_probability", fakeProbability);

        return restTemplate.postForObject(
                "http://localhost:5000/explain",
                request,
                Map.class
        );
    }
}
