package com.fakejobdetection.service;

import com.fakejobdetection.entity.AnalysisResult;
import com.fakejobdetection.ml.PredictionEngine;
import com.fakejobdetection.util.OpenNLPProcessor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TextAnalysisService {

    public AnalysisResult analyze(String text) {

        // --- NLP Feature Extraction ---
        List<String> tokens = OpenNLPProcessor.tokenize(text.toLowerCase());
        int sentenceCount = OpenNLPProcessor.sentenceCount(text);

        double keywordScore =
                tokens.stream().filter(t ->
                        t.equals("urgent") || t.equals("interview")).count() * 0.3;

        int urgentFlag = tokens.contains("urgent") ? 1 : 0;
        int noInterviewFlag = text.toLowerCase().contains("no interview") ? 1 : 0;

        Map<String, Object> features = new HashMap<>();
        features.put("keyword_score", keywordScore);
        features.put("sentence_count", sentenceCount);
        features.put("text_length", text.length());
        features.put("urgent_flag", urgentFlag);
        features.put("no_interview_flag", noInterviewFlag);

        // --- ML Prediction ---
        double fakeProbability =
                PredictionEngine.predictProbability(features);

        String label = fakeProbability >= 0.5 ? "FAKE" : "REAL";

        // --- Explanation (pre-LIME) ---
        List<String> reasons = new ArrayList<>();
        Map<String, Double> weights = new HashMap<>();

        if (urgentFlag == 1) {
            reasons.add("Urgent hiring language detected");
            weights.put("urgent_flag", 0.35);
        }
        if (noInterviewFlag == 1) {
            reasons.add("No interview mentioned");
            weights.put("no_interview_flag", 0.30);
        }

        if (reasons.isEmpty()) {
            reasons.add("No strong scam indicators found");
            weights.put("clean_text", 0.15);
        }

        // --- Build Result ---
        AnalysisResult result = new AnalysisResult();
        result.setInputText(text);

        AnalysisResult.Features f = new AnalysisResult.Features();
        f.salaryAnomaly = fakeProbability > 0.5;
        f.keywordScore = keywordScore;

        AnalysisResult.Prediction p = new AnalysisResult.Prediction();
        p.label = label;
        p.confidence = fakeProbability;

        AnalysisResult.Explanation e = new AnalysisResult.Explanation();
        e.reasons = reasons;
        e.weights = weights;

        result.setFeatures(f);
        result.setPrediction(p);
        result.setExplanation(e);

        return result;
    }
}
