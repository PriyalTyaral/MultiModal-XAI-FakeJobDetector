package com.example.random_major.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.random_major.entity.ExtractedData;
import com.example.random_major.entity.JobRecord;
import com.example.random_major.model.EnhancedJobResult;
import com.example.random_major.model.JobResult;
import com.example.random_major.service.EntityExtractionService;
import com.example.random_major.service.JobAnalysisService;
import com.example.random_major.service.LimeService;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class JobAnalysisController {

    @Autowired private JobAnalysisService jobService;
    @Autowired private LimeService limeService;
    @Autowired private EntityExtractionService entityExtractionService;
    @Autowired private com.example.random_major.repository.JobRecordRepository jobRecordRepository;
    @Autowired private com.example.random_major.service.JobResultService jobResultService;

    @Value("${lime.num-features:10}")
    private int defaultNumFeatures;

    @Value("${lime.output-format:json}")
    private String defaultOutputFormat;

    // ---------------------------------------------------------
    // ✅ TEXT ANALYSIS — with optional LIME depth / format params
    // ---------------------------------------------------------
    @PostMapping(value = "/analyze", consumes = "text/plain", produces = "application/json")
    public ResponseEntity<JobResult> analyzeJob(
            @RequestBody String jobText,
            @RequestParam(value = "numFeatures", defaultValue = "10") int numFeatures,
            @RequestParam(value = "format", defaultValue = "json") String format,
            @RequestParam(value = "userId", required = false) String userId
    ) {
        if (jobText == null || jobText.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new JobResult("error", 0.0, "Job text cannot be empty"));
        }
        JobResult result = jobService.analyzePlainText(jobText, numFeatures, format, userId);
        return ResponseEntity.ok(result);
    }

    // ---------------------------------------------------------
    // ✅ ENHANCED ANALYSIS — with company verification & domain validation
    // ---------------------------------------------------------
    /**
     * Enhanced analysis endpoint that validates company and domain before prediction
     * 
     * Request Parameters:
     * - jobText: The job posting text (required)
     * - companyName: The company name (REQUIRED - must be provided)
     * - jobPostingUrl: The job posting URL (optional)
     * - contactEmail: Contact email from job posting (optional)
     * - userId: User ID for tracking (optional)
     * 
     * @param jobText The job posting text
     * @param companyName The company name (MANDATORY)
     * @param jobPostingUrl The job posting URL
     * @param contactEmail Contact email
     * @param userId User ID
     * @return EnhancedJobResult with company verification, domain validation, and adjusted prediction
     */
    @PostMapping(value = "/analyze-enhanced", consumes = "text/plain", produces = "application/json")
    public ResponseEntity<?> analyzeJobEnhanced(
            @RequestBody String jobText,
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "jobPostingUrl", required = false) String jobPostingUrl,
            @RequestParam(value = "contactEmail", required = false) String contactEmail,
            @RequestParam(value = "userId", required = false) String userId
    ) {
        // ── Input validation ────────────────────────────────────────────
        if (jobText == null || jobText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Job text cannot be empty")
            );
        }

        try {
            // ✅ UPDATED: Use unified pipeline for consistent entity extraction
            // companyName is now OPTIONAL - will be auto-filled from extracted data if not provided
            EnhancedJobResult result = jobService.analyzeWithUnifiedPipeline(
                jobText,
                companyName,  // Can be null - will be auto-filled from extracted company
                jobPostingUrl,
                contactEmail,
                userId,
                "TEXT"  // Input type
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Analysis failed: " + e.getMessage())
            );
        }
    }

    // ---------------------------------------------------------
    // ✅ FILE ANALYSIS
    // ---------------------------------------------------------
    @PostMapping(value = "/analyze-file", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<?> analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "jobPostingUrl", required = false) String jobPostingUrl,
            @RequestParam(value = "contactEmail", required = false) String contactEmail
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Uploaded file is empty"));
        }

        String originalName = file.getOriginalFilename();
        String suffix = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".tmp";

        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", suffix);
            file.transferTo(tempFile);

            // ───────────────────────────────────────────────────────────
            // If verification parameters provided, use unified pipeline
            // Otherwise, use basic file analysis for backward compatibility
            // ───────────────────────────────────────────────────────────
            if (companyName != null && !companyName.trim().isEmpty()) {
                // Use unified pipeline with verification
                EnhancedJobResult result = jobService.analyzeFileWithUnifiedPipeline(
                    tempFile,
                    fileType,
                    companyName,
                    jobPostingUrl,
                    contactEmail,
                    userId
                );
                return ResponseEntity.ok(result);
            } else {
                // Use basic file analysis for backward compatibility
                EnhancedJobResult result = jobService.analyzeFromFile(tempFile, fileType, userId);
                return ResponseEntity.ok(result);
            }

        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ---------------------------------------------------------
    // ✅ FILE ANALYSIS WITH ENHANCED VERIFICATION
    // ---------------------------------------------------
    // Extracts text from file (IMAGE/AUDIO/DOCUMENT) and runs full
    // validation pipeline including company verification and domain validation
    // ---------------------------------------------------------
    @PostMapping(value = "/analyze-file-enhanced", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<?> analyzeFileEnhanced(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "jobPostingUrl", required = false) String jobPostingUrl,
            @RequestParam(value = "contactEmail", required = false) String contactEmail,
            @RequestParam(value = "userId", required = false) String userId
    ) throws IOException {
        // ─---- Input Validation ─────────────────────────────────────────
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Uploaded file is empty")
            );
        }

        if (fileType == null || fileType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "fileType parameter is required (audio, image, or document)")
            );
        }

        // Validate file type
        String normalizedFileType = fileType.toLowerCase();
        if (!normalizedFileType.matches("audio|image|document")) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid fileType. Must be 'audio', 'image', or 'document'")
            );
        }

        String originalName = file.getOriginalFilename();
        String suffix = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".tmp";

        File tempFile = null;
        try {
            // Create temporary file
            tempFile = File.createTempFile("upload_", suffix);
            file.transferTo(tempFile);

            // ───────────────────────────────────────────────────────────
            // Call unified pipeline for file analysis with verification
            // ───────────────────────────────────────────────────────────
            EnhancedJobResult result = jobService.analyzeFileWithUnifiedPipeline(
                tempFile,
                normalizedFileType,
                companyName,
                jobPostingUrl,
                contactEmail,
                userId
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                Map.of("error", "File analysis failed: " + e.getMessage())
            );
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ---------------------------------------------------------
    // ✅ ON-DEMAND LIME EXPLANATION (no re-prediction required)
    // Called when user adjusts depth slider on the ResultPage.
    // ---------------------------------------------------------
    @GetMapping(value = "/explain", produces = "application/json")
    public ResponseEntity<?> explainText(
            @RequestParam("text") String text,
            @RequestParam(value = "numFeatures", defaultValue = "10") int numFeatures,
            @RequestParam(value = "format", defaultValue = "json") String format
    ) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Parameter 'text' is required"));
        }
        try {
            LimeService.LimeResult limeResult = limeService.explain(text, numFeatures, format, null);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "lime_explanations", limeResult.explanations,
                    "cache_status", limeResult.cacheStatus,
                    "explanation_latency_ms", limeResult.latencyMs,
                    "gcs_url", limeResult.gcsUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "LIME explanation failed: " + e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // ✅ SAVE JOB RESULT (for persisting results to MongoDB)
    // ---------------------------------------------------------
    /**
     * Save an enhanced job result to MongoDB
     * Called after user completes analysis and wants to save for dashboard
     * 
     * POST /api/save-result
     * Body: { jobText, companyName, enhancedResult, inputType, userId }
     * 
     * @return Saved result details
     */
    @PostMapping("/save-result")
    public ResponseEntity<?> saveEnhancedResult(
        @RequestBody Map<String, Object> request
    ) {
        try {
            String jobText = (String) request.get("jobText");
            String companyName = (String) request.get("companyName");
            String inputType = (String) request.get("inputType");
            String userId = (String) request.get("userId");
            
            // Parse the enhanced result from request
            Map<String, Object> enhancedResultMap = (Map<String, Object>) request.get("enhancedResult");
            
            if (jobText == null || jobText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Job text is required"
                ));
            }
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User ID is required"
                ));
            }

            // Convert map to EnhancedJobResult (simplified parsing)
            EnhancedJobResult enhancedResult = new EnhancedJobResult(
                (String) enhancedResultMap.get("prediction"),
                ((Number) enhancedResultMap.get("confidenceScore")).doubleValue(),
                ((Number) enhancedResultMap.get("baseModelScore")).doubleValue()
            );
            
            enhancedResult.setAdjustmentFactor(((Number) enhancedResultMap.get("adjustmentFactor")).doubleValue());
            enhancedResult.setCompanyVerification(null); // Will be set from request
            enhancedResult.setDomainValidation(null); // Will be set from request
            enhancedResult.setExternalValidationInfluence((String) enhancedResultMap.get("externalValidationInfluence"));

            // Save to database
            JobRecord savedRecord = jobResultService.saveEnhancedJobResult(
                jobText,
                companyName,
                enhancedResult,
                inputType != null ? inputType : "TEXT",
                userId
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Result saved successfully",
                "resultId", savedRecord.getId(),
                "savedAt", savedRecord.getCreatedAt()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to save result: " + e.getMessage()
            ));
        }
    }

    // ---------------------------------------------------------
    // ✅ EXTRACT ENTITIES FROM TEXT
    // ---------------------------------------------------------
    /**
     * Extract company name, URL, and domain from job text
     * This endpoint auto-populates form fields without requiring full analysis
     * 
     * POST /api/extract-entities
     * Body: plain text (job posting text)
     * 
     * @param jobText The job posting text
     * @return ExtractedData { companyName, url, domain }
     */
    @PostMapping(value = "/extract-entities", consumes = "text/plain", produces = "application/json")
    public ResponseEntity<?> extractEntities(@RequestBody String jobText) {
        if (jobText == null || jobText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "success", false,
                    "error", "Job text cannot be empty",
                    "companyName", null,
                    "url", null,
                    "domain", null
                )
            );
        }

        try {
            ExtractedData extracted = entityExtractionService.extractFromText(jobText);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "companyName", extracted.getCompanyName() != null ? extracted.getCompanyName() : "",
                "url", extracted.getUrl() != null ? extracted.getUrl() : "",
                "domain", extracted.getDomain() != null ? extracted.getDomain() : ""
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of(
                    "success", false,
                    "error", "Entity extraction failed: " + e.getMessage(),
                    "companyName", null,
                    "url", null,
                    "domain", null
                )
            );
        }
    }
}