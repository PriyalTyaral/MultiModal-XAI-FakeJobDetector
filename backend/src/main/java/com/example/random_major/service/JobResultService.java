package com.example.random_major.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.random_major.entity.JobRecord;
import com.example.random_major.model.CompanyVerificationResponse;
import com.example.random_major.model.DomainValidationResponse;
import com.example.random_major.model.EnhancedJobResult;
import com.example.random_major.model.JobResult;
import com.example.random_major.repository.JobRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JobResultService: Handles saving job analysis results to MongoDB
 * 
 * Links results to users for dashboard retrieval
 * Stores all verification and validation data for historical reference
 */
@Service
public class JobResultService {

    private static final Logger log = LoggerFactory.getLogger(JobResultService.class);

    @Autowired
    private JobRecordRepository jobRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ───────────────────────────────────────────────────────
    // SAVE BASIC JOB RESULT
    // ───────────────────────────────────────────────────────
    /**
     * Saves a basic job result from plain text analysis
     * 
     * @param jobText The original job text
     * @param jobResult The analysis result
     * @param userId The user ID
     * @return Saved JobRecord
     */
    public JobRecord saveBasicJobResult(String jobText, JobResult jobResult, String userId) {
        try {
            JobRecord record = new JobRecord();
            record.setUserId(userId);
            record.setInputType("TEXT");
            record.setOriginalInput(jobText);
            record.setExtractedText(jobText); // For text input, extracted = original
            record.setPrediction(jobResult.getLabel());
            record.setConfidenceScore(jobResult.getProbabilityFake());
            record.setBaseModelScore(jobResult.getProbabilityFake());
            record.setAdjustmentFactor(0.0);
            record.setLimeExplanations(jobResult.getExplanation());

            JobRecord saved = jobRecordRepository.save(record);
            log.info("✅ Saved basic job result for user: {}", userId);
            return saved;

        } catch (Exception e) {
            log.error("❌ Failed to save basic job result: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save job result: " + e.getMessage());
        }
    }

    // ───────────────────────────────────────────────────────
    // SAVE ENHANCED JOB RESULT
    // ───────────────────────────────────────────────────────
    /**
     * Saves an enhanced job result with company verification and domain validation
     * 
     * @param jobText The original job text
     * @param companyName The company being verified
     * @param enhancedResult The enhanced analysis result
     * @param inputType The input type (TEXT, IMAGE, AUDIO, DOCUMENT)
     * @param userId The user ID
     * @return Saved JobRecord
     */
    public JobRecord saveEnhancedJobResult(
        String jobText,
        String companyName,
        EnhancedJobResult enhancedResult,
        String inputType,
        String userId
    ) {
        try {
            JobRecord record = new JobRecord();
            record.setUserId(userId);
            record.setInputType(inputType != null ? inputType : "TEXT");
            record.setOriginalInput(jobText);
            record.setExtractedText(jobText);
            record.setCompanyName(companyName);

            // ── Prediction Data ────────────────────────────────
            record.setPrediction(enhancedResult.getPrediction());
            record.setBaseModelScore(enhancedResult.getBaseModelScore());
            record.setConfidenceScore(enhancedResult.getConfidenceScore());
            record.setAdjustmentFactor(enhancedResult.getAdjustmentFactor());

            // ── Company Verification ──────────────────────────
            if (enhancedResult.getCompanyVerification() != null) {
                record.setCompanyVerification(
                    convertCompanyVerificationToMap(enhancedResult.getCompanyVerification())
                );
            }

            // ── Domain Validation ──────────────────────────────
            if (enhancedResult.getDomainValidation() != null) {
                record.setDomainValidation(
                    convertDomainValidationToMap(enhancedResult.getDomainValidation())
                );
            }

            // ── LIME Explanations ──────────────────────────────
            try {
                String explanationJson = objectMapper.writeValueAsString(enhancedResult.getLimeExplanations());
                record.setLimeExplanations(explanationJson);
            } catch (Exception e) {
                log.warn("Could not serialize LIME explanations: {}", e.getMessage());
                record.setLimeExplanations("[]");
            }

            // ── Metadata ───────────────────────────────────────
            record.setExternalValidationInfluence(enhancedResult.getExternalValidationInfluence());

            // ── Red Flags ──────────────────────────────────────
            record.setRedFlagScore(enhancedResult.getRedFlagScore());
            if (enhancedResult.getRedFlagsDetected() != null && !enhancedResult.getRedFlagsDetected().isEmpty()) {
                try {
                    String redFlagsJson = objectMapper.writeValueAsString(enhancedResult.getRedFlagsDetected());
                    record.setRedFlagsDetected(redFlagsJson);
                } catch (Exception e) {
                    log.warn("Could not serialize red flags: {}", e.getMessage());
                    record.setRedFlagsDetected("[]");
                }
            } else {
                record.setRedFlagsDetected("[]");
            }

            JobRecord saved = jobRecordRepository.save(record);
            log.info("✅ Saved enhanced job result for user: {} | Prediction: {} | Score: {}",
                userId, enhancedResult.getPrediction(), enhancedResult.getConfidenceScore());
            return saved;

        } catch (Exception e) {
            log.error("❌ Failed to save enhanced job result: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save job result: " + e.getMessage());
        }
    }

    // ───────────────────────────────────────────────────────
    // RETRIEVE USER RESULTS
    // ───────────────────────────────────────────────────────
    /**
     * Retrieves all job results for a user, sorted by creation date (newest first)
     * 
     * @param userId The user ID
     * @return List of JobRecords
     */
    public List<JobRecord> getUserJobResults(String userId) {
        try {
            List<JobRecord> results = jobRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.info("📋 Retrieved {} job results for user: {}", results.size(), userId);
            return results;

        } catch (Exception e) {
            log.error("❌ Failed to retrieve job results for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve job results: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific job result by ID
     * 
     * @param resultId The result ID
     * @return JobRecord or null if not found
     */
    public JobRecord getJobResultById(String resultId) {
        try {
            return jobRecordRepository.findById(resultId).orElse(null);

        } catch (Exception e) {
            log.error("❌ Failed to retrieve job result {}: {}", resultId, e.getMessage());
            throw new RuntimeException("Failed to retrieve job result: " + e.getMessage());
        }
    }

    // ───────────────────────────────────────────────────────
    // HELPER METHODS
    // ───────────────────────────────────────────────────────
    /**
     * Converts CompanyVerificationResponse to a Map for MongoDB storage
     * 
     * @param verification CompanyVerificationResponse
     * @return Map representation
     */
    private Map<String, Object> convertCompanyVerificationToMap(CompanyVerificationResponse verification) {
        Map<String, Object> map = new HashMap<>();
        map.put("exists", verification.isExists());
        map.put("status", verification.getStatus());
        map.put("website", verification.getWebsite());
        map.put("message", verification.getMessage());
        map.put("verificationMethod", verification.getVerificationMethod());
        map.put("apiCallTimeMs", verification.getApiCallTimeMs());
        return map;
    }

    /**
     * Converts DomainValidationResponse to a Map for MongoDB storage
     * 
     * @param domainValidation DomainValidationResponse
     * @return Map representation
     */
    private Map<String, Object> convertDomainValidationToMap(DomainValidationResponse domainValidation) {
        Map<String, Object> map = new HashMap<>();
        map.put("match", domainValidation.isMatch());
        map.put("riskScore", domainValidation.getRiskScore());
        map.put("extractedDomain", domainValidation.getExtractedDomain());
        map.put("companyDomain", domainValidation.getCompanyDomain());
        map.put("message", domainValidation.getMessage());
        return map;
    }

    /**
     * Calculates statistics for user's job results
     * 
     * @param userId The user ID
     * @return Statistics map
     */
    public Map<String, Object> getUserResultsStatistics(String userId) {
        try {
            List<JobRecord> results = getUserJobResults(userId);

            long totalResults = results.size();
            long fakeCount = results.stream().filter(r -> "FAKE".equals(r.getPrediction())).count();
            long realCount = results.stream().filter(r -> "REAL".equals(r.getPrediction())).count();

            double averageConfidence = results.stream()
                .mapToDouble(JobRecord::getConfidenceScore)
                .average()
                .orElse(0.0);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAnalyzed", totalResults);
            stats.put("fakeDetected", fakeCount);
            stats.put("realDetected", realCount);
            stats.put("averageConfidence", String.format("%.2f%%", averageConfidence * 100));
            stats.put("fakePercentage", totalResults > 0 ? String.format("%.1f%%", (fakeCount * 100.0) / totalResults) : "0%");

            log.info("📊 User {} statistics: {} total, {} fake, {} real", userId, totalResults, fakeCount, realCount);
            return stats;

        } catch (Exception e) {
            log.error("❌ Failed to calculate statistics for user {}: {}", userId, e.getMessage());
            return new HashMap<>();
        }
    }
}
