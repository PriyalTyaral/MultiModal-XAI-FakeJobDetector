package com.example.random_major.controller;

import com.example.random_major.model.JobResult;
import com.example.random_major.service.JobAnalysisService;
import com.example.random_major.service.LimeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.random_major.entity.JobRecord;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class JobAnalysisController {

    @Autowired private JobAnalysisService jobService;
    @Autowired private LimeService limeService;
    @Autowired private com.example.random_major.repository.JobRecordRepository jobRecordRepository;

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
    // ✅ FILE ANALYSIS
    // ---------------------------------------------------------
    @PostMapping(value = "/analyze-file", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<JobResult> analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "userId", required = false) String userId
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new JobResult("error", 0.0, "Uploaded file is empty"));
        }

        String originalName = file.getOriginalFilename();
        String suffix = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".tmp";

        File tempFile = File.createTempFile("upload_", suffix);
        file.transferTo(tempFile);

        JobResult result;
        try {
            result = jobService.analyzeFromFile(tempFile, fileType, userId);
        } finally {
            tempFile.delete();
        }
        return ResponseEntity.ok(result);
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
    // ✅ USER DASHBOARD — Get history for a specific user
    // ---------------------------------------------------------
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<?> getUserDashboard(@PathVariable("userId") String userId) {
        try {
            List<JobRecord> history = jobRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);

            long total = history.size();
            long fake = history.stream().filter(r -> "FAKE".equalsIgnoreCase(r.getResult())).count();
            long real = total - fake;
            double avgConfidence = history.stream()
                    .mapToDouble(JobRecord::getFakeConfidence)
                    .average()
                    .orElse(0.0);

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("fake", fake);
            stats.put("real", real);
            stats.put("avgConfidence", Math.round(avgConfidence * 10.0) / 10.0);

            Map<String, Object> response = new HashMap<>();
            response.put("stats", stats);
            response.put("history", history);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getClass().getName() + ": " + e.getMessage()));
        }
    }
}