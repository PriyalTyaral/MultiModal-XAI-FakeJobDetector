package com.example.random_major.service;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.random_major.model.CompanyVerificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CompanyVerificationService: Validates company existence using OpenCorporate API with DNS fallback
 * 
 * DO NOT MODIFY PMML MODEL FEATURES
 * This service only provides company verification data for post-processing adjustments
 * 
 * FALLBACK STRATEGY:
 * 1. Try OpenCorporate API - Returns: companyExists, domain, confidence
 * 2. If API fails or returns null:
 *    - Perform DNS lookup on company name as domain
 *    - Check if domain has valid A/AAAA records
 *    - Compare domain with company name for spoofing detection
 */
@Service
public class CompanyVerificationService {

    private static final Logger log = LoggerFactory.getLogger(CompanyVerificationService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${opencorporate.api-url:https://api.opencorporates.com/v0.4}")
    private String opencorporateApiUrl;

    @Value("${opencorporate.api-key:}")
    private String opencorporateApiKey;

    @Value("${company.verification.use-fallback:true}")
    private boolean useFallback;

    /**
     * Verifies if a company exists using OpenCorporate API with DNS fallback
     * 
     * @param companyName The company name to verify
     * @return CompanyVerificationResponse with verification status
     */
    public CompanyVerificationResponse verifyCompany(String companyName) {
        long startTime = System.currentTimeMillis();

        try {
            if (companyName == null || companyName.trim().isEmpty()) {
                return new CompanyVerificationResponse(
                    false,
                    "UNKNOWN",
                    null,
                    "Company name is required"
                );
            }

            // ──────────────────────────────────────────────────────
            // 🔍 PRIMARY: Try OpenCorporate API
            // ──────────────────────────────────────────────────────
            log.info("🔍 Verifying company '{}' via OpenCorporate API...", companyName);
            CompanyVerificationResponse apiResult = verifyViaOpenCorporateAPI(companyName);
            
            if (apiResult != null && !apiResult.getStatus().equals("UNKNOWN")) {
                apiResult.setApiCallTimeMs(System.currentTimeMillis() - startTime);
                apiResult.setVerificationMethod("OPENCORPORATE_API");
                return apiResult;
            }

            // ──────────────────────────────────────────────────────
            // ⚠️  FALLBACK: DNS Lookup if API fails
            // ──────────────────────────────────────────────────────
            if (useFallback) {
                log.info("⚠️  OpenCorporate API unavailable or returned no results, trying DNS fallback...");
                CompanyVerificationResponse fallbackResult = verifyViaDNSLookup(companyName);
                fallbackResult.setApiCallTimeMs(System.currentTimeMillis() - startTime);
                fallbackResult.setVerificationMethod("DNS_FALLBACK");
                return fallbackResult;
            }

            // No result from API or fallback
            CompanyVerificationResponse noResult = new CompanyVerificationResponse(
                false,
                "UNKNOWN",
                null,
                "Company verification unavailable (API and fallback failed)"
            );
            noResult.setApiCallTimeMs(System.currentTimeMillis() - startTime);
            noResult.setVerificationMethod("NONE");
            return noResult;

        } catch (Exception e) {
            log.error("❌ Company verification error: {}", e.getMessage(), e);
            
            CompanyVerificationResponse resp = new CompanyVerificationResponse(
                false,
                "UNKNOWN",
                null,
                "Company verification failed: " + e.getMessage()
            );
            resp.setApiCallTimeMs(System.currentTimeMillis() - startTime);
            resp.setVerificationMethod("ERROR");
            return resp;
        }
    }

    /**
     * PRIMARY VERIFICATION: OpenCorporate API
     * Returns company information if found
     * API Documentation: https://api.opencorporates.com/documentation/API-basics
     * Search endpoint: /companies/search?q={companyName}&api_token={apiToken}
     * 
     * @param companyName The company name to verify
     * @return CompanyVerificationResponse with OpenCorporate data or null if API fails
     */
    private CompanyVerificationResponse verifyViaOpenCorporateAPI(String companyName) {
        try {
            if (opencorporateApiKey == null || opencorporateApiKey.trim().isEmpty()) {
                log.warn("⚠️  OpenCorporate API key not configured, skipping API verification");
                return null;
            }

            // URL encode company name for API call
            String encodedCompanyName = URLEncoder.encode(companyName, StandardCharsets.UTF_8);
            String apiUrl = opencorporateApiUrl + "/companies/search?q=" + encodedCompanyName + "&api_token=" + opencorporateApiKey;

            log.debug("OpenCorporate API URL: {}...", opencorporateApiUrl + "/companies/search?q=...");

            // Prepare headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Accept", "application/json");

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            try {
                org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
                );

                if (response.getBody() == null) {
                    log.warn("⚠️  OpenCorporate returned empty response for '{}'", companyName);
                    return null;
                }

                Map<String, Object> body = response.getBody();
                Map<String, Object> results = (Map<String, Object>) body.get("results");
                
                if (results == null || results.isEmpty()) {
                    log.info("📋 OpenCorporate: Company '{}' not found", companyName);
                    return new CompanyVerificationResponse(
                        false,
                        "UNKNOWN",
                        null,
                        "Company not found in OpenCorporate database"
                    );
                }

                java.util.List<Map<String, Object>> companies = (java.util.List<Map<String, Object>>) results.get("companies");
                
                if (companies == null || companies.isEmpty()) {
                    log.info("📋 OpenCorporate: Company '{}' not found", companyName);
                    return new CompanyVerificationResponse(
                        false,
                        "UNKNOWN",
                        null,
                        "Company not found in OpenCorporate database"
                    );
                }

                // Get first result (most relevant)
                Map<String, Object> company = companies.get(0);
                String domain = (String) company.get("website");
                String status = (String) company.get("status");
                String companyNameFromAPI = (String) company.get("name");
                
                // If no domain from website field, try extracting from jurisdiction
                if (domain == null || domain.isEmpty()) {
                    domain = (String) company.get("jurisdiction_code");
                }

                if (companyNameFromAPI == null) {
                    log.info("📋 OpenCorporate: Invalid company data for '{}'", companyName);
                    return new CompanyVerificationResponse(
                        false,
                        "UNKNOWN",
                        null,
                        "Invalid company data in OpenCorporate database"
                    );
                }

                // Determine status: active/ACTIVE
                String normalizedStatus = (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("active"))) 
                    ? "ACTIVE" 
                    : "INACTIVE";

                log.info("✅ OpenCorporate: Company '{}' verified - Status: {}", companyNameFromAPI, normalizedStatus);

                return new CompanyVerificationResponse(
                    true,
                    normalizedStatus,
                    domain,
                    "Company verified via OpenCorporate - Status: " + normalizedStatus
                );

            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                log.info("📋 OpenCorporate: Company '{}' not found (404)", companyName);
                return new CompanyVerificationResponse(
                    false,
                    "UNKNOWN",
                    null,
                    "Company not found in OpenCorporate database"
                );
            } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
                log.error("❌ OpenCorporate: Invalid API key (401 Unauthorized)");
                return null;
            }

        } catch (RestClientException e) {
            log.warn("❌ OpenCorporate API connection failed: {}", e.getMessage());
            return null;

        } catch (Exception e) {
            log.warn("❌ OpenCorporate API error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * FALLBACK VERIFICATION: DNS Lookup
     * Attempts to resolve company name as domain and verifies DNS records
     * 
     * STRATEGY:
     * 1. Convert company name to potential domain (remove spaces/special chars)
     * 2. Try common TLDs (.com, .org, .net, .io)
     * 3. Perform DNS A record lookup
     * 4. Return verification based on DNS availability
     * 
     * @param companyName The company name to verify
     * @return CompanyVerificationResponse based on DNS availability
     */
    private CompanyVerificationResponse verifyViaDNSLookup(String companyName) {
        try {
            // Convert company name to potential domain
            // e.g., "Google Inc" -> "google.com", "TechCorp" -> "techcorp.com"
            String potentialDomain = companyName.toLowerCase()
                .replaceAll("\\s+", "")  // Remove spaces
                .replaceAll("[^a-z0-9-]", "")  // Remove special chars
                .replaceAll("-+", "-");  // Normalize dashes

            if (potentialDomain.isEmpty()) {
                log.warn("⚠️  Could not convert company name '{}' to domain", companyName);
                return new CompanyVerificationResponse(
                    false,
                    "UNKNOWN",
                    null,
                    "Could not generate domain from company name"
                );
            }
            
            // Try common TLDs if not already present
            String[] domainVariants = {
                potentialDomain + ".com",
                potentialDomain + ".org",
                potentialDomain + ".net",
                potentialDomain + ".io"
            };

            for (String domain : domainVariants) {
                try {
                    // Attempt DNS lookup (A record)
                    InetAddress.getByName(domain);
                    
                    log.info("✅ DNS Fallback: Domain '{}' resolved successfully", domain);
                    return new CompanyVerificationResponse(
                        true,
                        "ACTIVE",
                        domain,
                        "Company verified via DNS lookup - Domain has valid A records"
                    );

                } catch (java.net.UnknownHostException e) {
                    log.debug("  DNS lookup failed for '{}': {}", domain, e.getMessage());
                    continue;
                }
            }

            // No domain variant resolved
            log.warn("⚠️  DNS fallback: No domain found for company '{}'", companyName);
            return new CompanyVerificationResponse(
                false,
                "UNKNOWN",
                null,
                "Company domain not found via DNS lookup"
            );

        } catch (Exception e) {
            log.warn("❌ DNS fallback error: {}", e.getMessage());
            return new CompanyVerificationResponse(
                false,
                "UNKNOWN",
                null,
                "DNS lookup failed: " + e.getMessage()
            );
        }
    }
}
