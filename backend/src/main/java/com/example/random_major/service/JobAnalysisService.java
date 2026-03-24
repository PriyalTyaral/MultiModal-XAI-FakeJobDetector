package com.example.random_major.service;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.random_major.entity.JobRecord;
import com.example.random_major.model.JobResult;
import com.example.random_major.repository.JobRecordRepository;

@Service
public class JobAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(JobAnalysisService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private ModelEvaluatorService modelEvaluatorService;
    @Autowired private JobRecordRepository jobRecordRepository;
    @Autowired private OcrService ocrService;
    @Autowired private TextExtractService textExtractService;
    @Autowired private AudioService audioService;
    @Autowired private LimeService limeService;

    @Value("${lime.num-features:10}")
    private int defaultNumFeatures;

    @Value("${lime.output-format:json}")
    private String defaultOutputFormat;

    // ---------------------------------------------------
    // ✅ TEXT ANALYSIS (PMML + LIME)
    // ---------------------------------------------------
    public JobResult analyzePlainText(String jobText) {
        return analyzePlainText(jobText, defaultNumFeatures, defaultOutputFormat, null);
    }

    public JobResult analyzePlainText(String jobText, int numFeatures, String outputFormat, String userId) {
        try {
            // ── Step 1: PMML Prediction ──────────────────────────
            Map<String, Object> result = modelEvaluatorService.predict(jobText);

            double probabilityFake =
                    ((Number) result.getOrDefault("probability_fake", 0.0)).doubleValue();
            double confidence = probabilityFake * 100;
            String finalLabel = probabilityFake >= 0.5 ? "FAKE" : "REAL";

            // ── Step 2: LIME Explanation ─────────────────────────
            LimeService.LimeResult limeResult;
            try {
                limeResult = limeService.explain(jobText, numFeatures, outputFormat, userId);
                log.info("LIME returned {} features, status={}, latency={}ms",
                        limeResult.explanations.size(), limeResult.cacheStatus, limeResult.latencyMs);
            } catch (Exception e) {
                log.error("LIME call failed unexpectedly: {}", e.getMessage());
                limeResult = LimeService.LimeResult.error(0);
            }

            // ── Step 3: Serialize explanation for MongoDB ─────────
            String explanationJson = "[]";
            try {
                explanationJson = objectMapper.writeValueAsString(limeResult.explanations);
            } catch (Exception ignored) {}

            // ── Step 4: Save to MongoDB ───────────────────────────
            JobRecord record = new JobRecord(jobText, finalLabel, confidence, explanationJson, userId);
            record.setGcsUrl(limeResult.gcsUrl);
            jobRecordRepository.save(record);

            // ── Step 5: Build enriched JobResult ──────────────────
            JobResult jobResult = new JobResult(finalLabel, probabilityFake, explanationJson);
            jobResult.setLimeExplanations(limeResult.explanations);
            jobResult.setCacheStatus(limeResult.cacheStatus);
            jobResult.setExplanationLatencyMs(limeResult.latencyMs);
            jobResult.setGcsUrl(limeResult.gcsUrl);
            jobResult.setJobText(jobText); // ✅ Pass text back for UI depth re-fetch

            return jobResult;

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage(), e);
            return new JobResult("error", 0.0, "Model evaluation failed");
        }
    }

    // ---------------------------------------------------
    // ✅ FILE ANALYSIS
    // ---------------------------------------------------
    public JobResult analyzeFromFile(File file, String fileType, String userId) {
        try {
            String extractedText = null;

            if (fileType.equalsIgnoreCase("audio")) {
                extractedText = audioService.transcribeAudio(file);
            } else if (fileType.equalsIgnoreCase("image")) {
                extractedText = ocrService.extractTextFromImage(file);
            } else if (fileType.equalsIgnoreCase("document")) {
                extractedText = textExtractService.extractText(file);
            } else {
                return new JobResult("error", 0.0, "Unsupported file type");
            }

            if (extractedText == null || extractedText.trim().length() < 20) {
                return new JobResult("error", 0.0, "Unable to extract readable text from file");
            }

            log.info("Extracted text length: {}", extractedText.length());
            return analyzePlainText(extractedText, defaultNumFeatures, defaultOutputFormat, userId);

        } catch (Exception e) {
            log.error("File processing failed: {}", e.getMessage(), e);
            return new JobResult("error", 0.0, "File processing failed");
        }
    }
}