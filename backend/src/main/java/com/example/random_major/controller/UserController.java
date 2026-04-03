package com.example.random_major.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.random_major.entity.JobRecord;
import com.example.random_major.entity.User;
import com.example.random_major.model.JobResultDTO;
import com.example.random_major.service.JobResultService;
import com.example.random_major.service.UserService;

/**
 * UserController: Handles user authentication and dashboard endpoints
 */
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JobResultService jobResultService;

    /**
     * Register a new user
     * 
     * POST /api/users/signup
     * Body: { name, email, password }
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            
            User user = userService.signup(name, email, password);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User registered successfully",
                "userId", user.getId(),
                "name", user.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Login user
     * 
     * POST /api/users/signin
     * Body: { email, password }
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            User user = userService.signin(email, password);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged in successfully",
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get user dashboard with statistics
     * 
     * GET /api/users/{userId}/dashboard
     * 
     * @param userId The user ID
     * @return Dashboard data with stats and recent results
     */
    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable String userId) {
        try {
            // Get statistics
            Map<String, Object> statistics = jobResultService.getUserResultsStatistics(userId);

            // Get recent results (limit to 10)
            List<JobRecord> results = jobResultService.getUserJobResults(userId);
            List<JobResultDTO> recentResults = results.stream()
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", statistics,
                "recentResults", recentResults
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load dashboard: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all job results for user
     * 
     * GET /api/users/{userId}/results
     * 
     * @param userId The user ID
     * @return List of all job results
     */
    @GetMapping("/{userId}/results")
    public ResponseEntity<?> getUserResults(@PathVariable String userId) {
        try {
            List<JobRecord> results = jobResultService.getUserJobResults(userId);
            List<JobResultDTO> resultDTOs = results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", results.size(),
                "results", resultDTOs
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load results: " + e.getMessage()
            ));
        }
    }

    /**
     * Get a specific job result by ID
     * 
     * GET /api/users/{userId}/results/{resultId}
     * 
     * @param userId The user ID
     * @param resultId The result ID
     * @return Job result details
     */
    @GetMapping("/{userId}/results/{resultId}")
    public ResponseEntity<?> getJobResultDetail(
        @PathVariable String userId,
        @PathVariable String resultId
    ) {
        try {
            JobRecord result = jobResultService.getJobResultById(resultId);

            if (result == null) {
                return ResponseEntity.notFound().build();
            }

            // Verify user own this result
            if (!userId.equals(result.getUserId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Unauthorized to access this result"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "result", convertToDTO(result)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to load result: " + e.getMessage()
            ));
        }
    }

    /**
     * Convert JobRecord entity to JobResultDTO for API responses
     * 
     * @param record The JobRecord entity
     * @return JobResultDTO
     */
    private JobResultDTO convertToDTO(JobRecord record) {
        JobResultDTO dto = new JobResultDTO(
            record.getId(),
            record.getInputType(),
            record.getCompanyName(),
            record.getPrediction(),
            record.getConfidenceScore(),
            record.getBaseModelScore(),
            record.getAdjustmentFactor(),
            record.getCompanyVerification(),
            record.getDomainValidation(),
            record.getExternalValidationInfluence(),
            record.getCreatedAt(),
            record.getOriginalInput()
        );

        // Parse red flags from JSON string
        dto.setRedFlagScore(record.getRedFlagScore());
        if (record.getRedFlagsDetected() != null && !record.getRedFlagsDetected().isEmpty()) {
            try {
                java.util.List<com.example.random_major.model.RedFlag> redFlags = 
                    java.util.Arrays.asList(
                        new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            record.getRedFlagsDetected(),
                            com.example.random_major.model.RedFlag[].class
                        )
                    );
                dto.setRedFlagsDetected(redFlags);
            } catch (Exception e) {
                dto.setRedFlagsDetected(new java.util.ArrayList<>());
            }
        } else {
            dto.setRedFlagsDetected(new java.util.ArrayList<>());
        }

        return dto;
    }
}
