package com.example.random_major.service;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.random_major.entity.ExtractedData;
import com.example.random_major.entity.JobRecord;
import com.example.random_major.model.CompanyVerificationResponse;
import com.example.random_major.model.DomainValidationResponse;
import com.example.random_major.model.EnhancedJobResult;
import com.example.random_major.model.JobResult;
import com.example.random_major.repository.JobRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JobAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(JobAnalysisService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private ModelEvaluatorService modelEvaluatorService;
    @Autowired private JobRecordRepository jobRecordRepository;
    @Autowired private JobResultService jobResultService;
    @Autowired private OcrService ocrService;
    @Autowired private TextExtractService textExtractService;
    @Autowired private AudioService audioService;
    @Autowired private LimeService limeService;
    @Autowired private CompanyVerificationService companyVerificationService;
    @Autowired private DomainValidationService domainValidationService;
    @Autowired private PredictionService predictionService;
    @Autowired private RedFlagDetectionService redFlagDetectionService;
    @Autowired private EntityExtractionService entityExtractionService;

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
            } catch (JsonProcessingException ignored) {}

            // ── Step 4: Save to MongoDB ───────────────────────────
            JobRecord record = new JobRecord(jobText, finalLabel, confidence, explanationJson, userId);
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
    // ✅ FILE ANALYSIS (with unified pipeline)
    // ---------------------------------------------------
    public EnhancedJobResult analyzeFromFile(File file, String fileType, String userId) {
        try {
            log.info("📁 Processing file upload - Type: {}", fileType);
            String extractedText;

            if (fileType.equalsIgnoreCase("audio")) {
                log.info("🎤 Transcribing audio...");
                extractedText = audioService.transcribeAudio(file);
            } else if (fileType.equalsIgnoreCase("image")) {
                log.info("🖼️  Extracting text from image via OCR...");
                extractedText = ocrService.extractTextFromImage(file);
            } else if (fileType.equalsIgnoreCase("document")) {
                log.info("📄 Extracting text from document...");
                extractedText = textExtractService.extractText(file);
            } else {
                log.error("❌ Unsupported file type: {}", fileType);
                EnhancedJobResult errorResult = new EnhancedJobResult("error", 0.0, 0.0);
                return errorResult;
            }

            if (extractedText == null || extractedText.trim().length() < 20) {
                log.warn("❌ Insufficient text extracted from file (length: {})", 
                    extractedText != null ? extractedText.length() : 0);
                EnhancedJobResult errorResult = new EnhancedJobResult("error", 0.0, 0.0);
                return errorResult;
            }

            log.info("✅ Text extracted successfully - Length: {} characters", extractedText.length());
            
            // ✅ UNIFIED PIPELINE: Call enhanced analysis with auto-extracted entities
            return analyzeWithUnifiedPipeline(extractedText, null, null, null, userId, fileType);

        } catch (Exception e) {
            log.error("❌ File processing failed: {}", e.getMessage(), e);
            EnhancedJobResult errorResult = new EnhancedJobResult("error", 0.0, 0.0);
            return errorResult;
        }
    }

    // ---------------------------------------------------
    // ✅ ENTITY EXTRACTION (Company Name, URL, Domain)
    // ---------------------------------------------------
    /**
     * Extracts structured information from job text using EntityExtractionService
     * 
     * This method should be called after text extraction (OCR/transcription)
     * to auto-fill company name, URL, and domain fields
     * 
     * @param jobText The clean extracted text
     * @return ExtractedData with companyName, url, domain
     */
    public ExtractedData extractEntities(String jobText) {
        try {
            log.info("🔍 Extracting entities from job text...");
            ExtractedData extractedData = entityExtractionService.extractFromText(jobText);
            
            log.info("✅ Entity extraction completed - Company: '{}', URL: '{}', Domain: '{}'",
                    extractedData.getCompanyName(), 
                    extractedData.getUrl(), 
                    extractedData.getDomain());
            
            return extractedData;
        } catch (Exception e) {
            log.error("Entity extraction failed: {}", e.getMessage(), e);
            return new ExtractedData(null, null, null);
        }
    }

    // ---------------------------------------------------
    // ✅ ENHANCED TEXT ANALYSIS (with company verification & domain validation)
    // ---------------------------------------------------
    /**
     * Enhanced analysis with company verification and post-processing
     * Delegates to unified pipeline with user-provided company info
     * 
     * @param jobText The job posting text
     * @param companyName The company name (optional - will be auto-detected if empty)
     * @param jobPostingUrl The job posting URL (optional)
     * @param contactEmail The contact email (optional)
     * @param userId The user ID (optional)
     * @return EnhancedJobResult with verification and adjusted prediction
     */
    public EnhancedJobResult analyzeWithCompanyVerification(
            String jobText,
            String companyName,
            String jobPostingUrl,
            String contactEmail,
            String userId
    ) {
        log.info("🔍 Starting enhanced analysis with company verification...");
        return (EnhancedJobResult) analyzeWithUnifiedPipeline(
            jobText, 
            companyName, 
            jobPostingUrl, 
            contactEmail, 
            userId, 
            "TEXT"
        );
    }

    // ---------------------------------------------------
    // ✅ UNIFIED PROCESSING PIPELINE (for ALL input types)
    // ---------------------------------------------------
    /**
     * UNIFIED PIPELINE that processes all input types through a consistent flow:
     * 1. Extract entities from text (MANDATORY for all)
     * 2. Auto-fill company name if user didn't provide one
     * 3. Run company verification
     * 4. Run domain validation
     * 5. Get ML prediction with LIME explanation
     * 6. Apply red flag detection and post-processing
     * 
     * @param jobText The extracted/input job text
     * @param userCompanyName Company name entered by user (optional)
     * @param userJobPostingUrl URL entered by user (optional)
     * @param userContactEmail Contact email entered by user (optional)
     * @param userId User ID (optional)
     * @param inputType Type of input (TEXT, IMAGE, AUDIO, DOCUMENT)
     * @return EnhancedJobResult with all validations and extracted data
     */
    public EnhancedJobResult analyzeWithUnifiedPipeline(
            String jobText,
            String userCompanyName,
            String userJobPostingUrl,
            String userContactEmail,
            String userId,
            String inputType
    ) {
        try {
            log.info("═══════════════════════════════════════════════════════════");
            log.info("🔄 UNIFIED PIPELINE: Starting analysis for {} input", 
                inputType != null ? inputType : "TEXT");
            log.info("═══════════════════════════════════════════════════════════");
            
            if (jobText == null || jobText.trim().isEmpty()) {
                log.error("❌ Job text cannot be null or empty");
                return new EnhancedJobResult("error", 0.0, 0.0);
            }

            // ═══════════════════════════════════════════════════════════
            // STEP 1: ENTITY EXTRACTION (MANDATORY for all input types)
            // ═══════════════════════════════════════════════════════════
            log.info("📋 STEP 1: Extracting entities from text...");
            ExtractedData extractedData = entityExtractionService.extractFromText(jobText);
            log.info("✅ Entity extraction completed");
            log.info("   - Company: '{}'", extractedData.getCompanyName());
            log.info("   - URL: '{}'", extractedData.getUrl());
            log.info("   - Domain: '{}'", extractedData.getDomain());

            // ═══════════════════════════════════════════════════════════
            // STEP 2: AUTO-FILL company name (use extracted if user didn't provide)
            // ═══════════════════════════════════════════════════════════
            log.info("🔍 STEP 2: Determining company name to use for validation...");
            String companyNameForValidation = userCompanyName;
            
            if (companyNameForValidation == null || companyNameForValidation.trim().isEmpty()) {
                companyNameForValidation = extractedData.getCompanyName();
                log.info("✅ Using extracted company name: '{}'", companyNameForValidation);
            } else {
                log.info("✅ Using user-provided company name: '{}'", userCompanyName);
            }

            // Determine URL and email to use
            String urlForValidation = userJobPostingUrl != null ? userJobPostingUrl : extractedData.getUrl();
            String emailForValidation = userContactEmail != null ? userContactEmail : extractedData.getDomain();

            // ═══════════════════════════════════════════════════════════
            // STEP 3: ML PREDICTION (base score from PMML model)
            // ═══════════════════════════════════════════════════════════
            log.info("📊 STEP 3: Running ML model prediction...");
            Map<String, Object> result = modelEvaluatorService.predict(jobText);
            double baseModelScore = 
                    ((Number) result.getOrDefault("probability_fake", 0.0)).doubleValue();
            log.info("✅ Base model score: {} ({}%)", baseModelScore, (int)(baseModelScore * 100));

            // ═══════════════════════════════════════════════════════════
            // STEP 4: RED FLAG DETECTION
            // ═══════════════════════════════════════════════════════════
            log.info("🚩 STEP 4: Running red flag detection...");
            java.util.List<com.example.random_major.model.RedFlag> redFlags = 
                    redFlagDetectionService.detectRedFlags(jobText);
            double redFlagScore = redFlagDetectionService.calculateRedFlagScore(redFlags);
            log.info("✅ Red flag detection completed: {} flags detected, score: {}", 
                redFlags.size(), redFlagScore);

            // ═══════════════════════════════════════════════════════════
            // STEP 5: COMPANY VERIFICATION
            // ═══════════════════════════════════════════════════════════
            log.info("🏢 STEP 5: Verifying company: '{}'", companyNameForValidation);
            CompanyVerificationResponse companyVerification = null;
            
            if (companyNameForValidation != null && !companyNameForValidation.isEmpty()) {
                companyVerification = companyVerificationService.verifyCompany(companyNameForValidation);
            }
            
            if (companyVerification == null) {
                companyVerification = new CompanyVerificationResponse(
                    false, "UNKNOWN", null, "Company verification unavailable or not performed"
                );
            }
            log.info("✅ Company verification completed - Status: {}", companyVerification.getStatus());

            // ═══════════════════════════════════════════════════════════
            // STEP 6: DOMAIN VALIDATION
            // ═══════════════════════════════════════════════════════════
            log.info("🔗 STEP 6: Validating domain...");
            DomainValidationResponse domainValidation = null;
            
            String companyDomainForValidation = null;
            if (companyVerification.isExists() && companyVerification.getWebsite() != null) {
                companyDomainForValidation = companyVerification.getWebsite();
                log.info("   Using verified company domain: {}", companyDomainForValidation);
            } else {
                log.info("   Company not verified, will validate against posting domains...");
            }
            
            // Always attempt domain validation if URL or email is provided
            if ((urlForValidation != null && !urlForValidation.isEmpty()) || 
                (emailForValidation != null && !emailForValidation.isEmpty())) {
                domainValidation = domainValidationService.validateDomain(
                    companyDomainForValidation,
                    urlForValidation,
                    emailForValidation
                );
                log.info("✅ Domain validation completed");
            } else {
                log.info("⚠️  No URL or email to validate");
            }

            // ═══════════════════════════════════════════════════════════
            // STEP 7: POST-PROCESSING (adjust score based on validation)
            // ═══════════════════════════════════════════════════════════
            log.info("⚙️  STEP 7: Applying post-processing adjustments...");
            PredictionService.PostProcessingResult postProcessing = 
                    predictionService.applyPostProcessing(
                        baseModelScore,
                        companyVerification,
                        domainValidation
                    );
            
            double postProcessedScore = postProcessing.getAdjustedScore();
            double adjustmentFactor = postProcessing.getAdjustmentFactor();
            
            if (redFlagScore > 0) {
                postProcessedScore = redFlagDetectionService.applyRedFlagScaling(postProcessedScore, redFlagScore);
                log.info("   Applied red flag scaling: {} → {}", 
                    postProcessing.getAdjustedScore(), postProcessedScore);
            }
            log.info("✅ Adjustment factor: {}, Final score: {} ({}%)", 
                adjustmentFactor, postProcessedScore, (int)(postProcessedScore * 100));

            // ═══════════════════════════════════════════════════════════
            // STEP 8: LIME EXPLANATION (interpretability)
            // ═══════════════════════════════════════════════════════════
            log.info("💡 STEP 8: Generating LIME explanations...");
            LimeService.LimeResult limeResult;
            try {
                limeResult = limeService.explain(jobText, defaultNumFeatures, defaultOutputFormat, userId);
                log.info("✅ LIME explanation completed - {} features, status: {}", 
                    limeResult.explanations.size(), limeResult.cacheStatus);
            } catch (Exception e) {
                log.error("⚠️  LIME explanation failed: {}", e.getMessage());
                limeResult = LimeService.LimeResult.error(0);
            }

            // ═══════════════════════════════════════════════════════════
            // STEP 9: BUILD RESPONSE
            // ═══════════════════════════════════════════════════════════
            log.info("📦 STEP 9: Building response object...");
            double finalScore = postProcessedScore;
            String finalPrediction = finalScore >= 0.5 ? "FAKE" : "REAL";

            EnhancedJobResult enhancedResult = new EnhancedJobResult(
                finalPrediction,
                finalScore,
                baseModelScore
            );
            
            // Set all response fields
            enhancedResult.setAdjustmentFactor(adjustmentFactor);
            enhancedResult.setCompanyVerification(companyVerification);
            enhancedResult.setDomainValidation(domainValidation);
            enhancedResult.setLimeExplanations(limeResult.explanations);
            enhancedResult.setRedFlagScore(redFlagScore);
            enhancedResult.setRedFlagsDetected(redFlags);
            enhancedResult.setExternalValidationInfluence(
                postProcessing.getExternalValidationNote() + "\n" +
                redFlagDetectionService.formatRedFlagsForNote(redFlags, redFlagScore)
            );
            enhancedResult.setCacheStatus(limeResult.cacheStatus);
            enhancedResult.setExplanationLatencyMs(limeResult.latencyMs);
            enhancedResult.setGcsUrl(limeResult.gcsUrl);
            
            // ✅ SET EXTRACTED DATA IN RESPONSE
            enhancedResult.setExtractedCompanyName(extractedData.getCompanyName());
            enhancedResult.setExtractedUrl(extractedData.getUrl());
            enhancedResult.setExtractedDomain(extractedData.getDomain());
            
            log.info("✅ Response object built");

            // ═══════════════════════════════════════════════════════════
            // STEP 10: SAVE TO DATABASE
            // ═══════════════════════════════════════════════════════════
            log.info("💾 STEP 10: Saving to database...");
            try {
                jobResultService.saveEnhancedJobResult(
                    jobText,
                    companyNameForValidation,
                    enhancedResult,
                    inputType != null ? inputType : "TEXT",
                    userId
                );
                log.info("✅ Saved to MongoDB");
            } catch (Exception e) {
                log.warn("⚠️  Could not save to database: {}", e.getMessage());
            }

            // ═══════════════════════════════════════════════════════════
            // SUMMARY
            // ═══════════════════════════════════════════════════════════
            log.info("═══════════════════════════════════════════════════════════");
            log.info("✅ PIPELINE COMPLETE");
            log.info("   Input Type: {}", inputType);
            log.info("   Prediction: {} (confidence: {}%)", finalPrediction, (int)(finalScore * 100));
            log.info("   Company: {} (Status: {})", companyNameForValidation, companyVerification.getStatus());
            log.info("   Red Flags: {}", redFlags.size());
            log.info("   Adjustment: {} (base: {} → final: {}%)", 
                adjustmentFactor, (int)(baseModelScore * 100), (int)(finalScore * 100));
            log.info("═══════════════════════════════════════════════════════════");

            return enhancedResult;

        } catch (Exception e) {
            log.error("❌ PIPELINE FAILED: {}", e.getMessage(), e);
            EnhancedJobResult errorResult = new EnhancedJobResult("error", 0.0, 0.0);
            return errorResult;
        }
    }

    // ---------------------------------------------------
    // ✅ FILE ANALYSIS WITH UNIFIED PIPELINE
    // ---------------------------------------------------
    /**
     * Combined method that extracts file text and runs the unified pipeline
     * This ensures entity extraction and validation for IMAGE/AUDIO/DOCUMENT inputs
     * 
     * @param file The uploaded file
     * @param fileType File type: audio, image, or document
     * @param userCompanyName User-provided company name (optional)
     * @param userJobPostingUrl User-provided job posting URL (optional)
     * @param userContactEmail User-provided contact email (optional)
     * @param userId User ID (optional)
     * @return EnhancedJobResult with all validations performed
     */
    public EnhancedJobResult analyzeFileWithUnifiedPipeline(
            File file,
            String fileType,
            String userCompanyName,
            String userJobPostingUrl,
            String userContactEmail,
            String userId
    ) {
        try {
            log.info("🔄 FILE ANALYSIS WITH UNIFIED PIPELINE");
            log.info("   File Type: {}", fileType);
            
            // ──────────────────────────────────────────────────────────
            // STEP 1: Extract text from file
            // ──────────────────────────────────────────────────────────
            log.info("📄 STEP 1: Extracting text from file...");
            String extractedText = null;

            if (fileType != null && fileType.equalsIgnoreCase("audio")) {
                log.info("   Using audio transcription service...");
                extractedText = audioService.transcribeAudio(file);
            } else if (fileType != null && fileType.equalsIgnoreCase("image")) {
                log.info("   Using OCR service...");
                extractedText = ocrService.extractTextFromImage(file);
            } else if (fileType != null && fileType.equalsIgnoreCase("document")) {
                log.info("   Using document text extraction service...");
                extractedText = textExtractService.extractText(file);
            } else {
                log.error("❌ Unsupported file type: {}", fileType);
                EnhancedJobResult errorResult = new EnhancedJobResult("error", 0.0, 0.0);
                return errorResult;
            }

            if (extractedText == null || extractedText.trim().length() < 20) {
                log.error("❌ Unable to extract readable text from file");
                EnhancedJobResult errorResult = new EnhancedJobResult("error", 0.0, 0.0);
                return errorResult;
            }

            log.info("✅ Text extracted successfully ({} characters)", extractedText.length());

            // ──────────────────────────────────────────────────────────
            // STEP 2: Call unified pipeline with extracted text
            // ──────────────────────────────────────────────────────────
            log.info("🔄 STEP 2: Running unified pipeline with extracted text...");
            EnhancedJobResult result = analyzeWithUnifiedPipeline(
                extractedText,
                userCompanyName,
                userJobPostingUrl,
                userContactEmail,
                userId,
                fileType.toUpperCase()
            );

            return result;

        } catch (Exception e) {
            log.error("❌ FILE ANALYSIS WITH UNIFIED PIPELINE FAILED: {}", e.getMessage(), e);
            EnhancedJobResult errorResult = new EnhancedJobResult("error", 0.0, 0.0);
            return errorResult;
        }
    }
}