package com.example.random_major.service;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.random_major.model.CompanyVerificationResponse;

@Service
public class CompanyVerificationService {

    private static final Logger log = LoggerFactory.getLogger(CompanyVerificationService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${opencorporate.api-url}")
    private String baseUrl;

    @Value("${opencorporate.api-key:}")
    private String apiKey;

    @Value("${company.verification.use-fallback:true}")
    private boolean useFallback;

    public CompanyVerificationResponse verifyCompany(String companyName) {

        long startTime = System.currentTimeMillis();

        try {
            if (companyName == null || companyName.trim().isEmpty()) {
                return new CompanyVerificationResponse(false, "UNKNOWN", null, "Company name missing");
            }

            // 🔍 STEP 1: OpenCorporates API
            CompanyVerificationResponse apiResult = verifyViaAPI(companyName);

            if (apiResult != null) {
                apiResult.setVerificationMethod("OPENCORPORATE_API");
                apiResult.setApiCallTimeMs(System.currentTimeMillis() - startTime);
                return apiResult;
            }

            // ⚠️ STEP 2: DNS FALLBACK
            if (useFallback) {
                CompanyVerificationResponse dnsResult = verifyViaDNS(companyName);
                dnsResult.setVerificationMethod("DNS_FALLBACK");
                dnsResult.setApiCallTimeMs(System.currentTimeMillis() - startTime);
                return dnsResult;
            }

            return new CompanyVerificationResponse(false, "UNKNOWN", null, "Verification failed");

        } catch (Exception e) {
            log.error("❌ Error verifying company: {}", e.getMessage());
            return new CompanyVerificationResponse(false, "UNKNOWN", null, "Error occurred");
        }
    }

    // ===============================
    // 🔍 OPEN CORPORATES API METHOD
    // ===============================
    private CompanyVerificationResponse verifyViaAPI(String companyName) {

        try {
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("⚠️ API key missing, skipping API");
                return null;
            }

            String encodedName = URLEncoder.encode(companyName, StandardCharsets.UTF_8);

            String url = baseUrl + "/companies/search?q=" + encodedName + "&api_token=" + apiKey;

            log.info("🔍 Calling OpenCorporates API for: {}", companyName);

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null) return null;

            Map results = (Map) response.get("results");
            if (results == null) return null;

            List companies = (List) results.get("companies");
            if (companies == null || companies.isEmpty()) {
                return new CompanyVerificationResponse(false, "UNKNOWN", null, "Company not found");
            }

            // Extract first company
            Map companyWrapper = (Map) companies.get(0);
            Map company = (Map) companyWrapper.get("company");

            if (company == null) return null;

            String name = (String) company.get("name");
            String status = (String) company.get("current_status");
            String registryUrl = (String) company.get("registry_url");

            String normalizedStatus = "UNKNOWN";

            if (status != null) {
                if (status.toLowerCase().contains("active")) {
                    normalizedStatus = "ACTIVE";
                } else {
                    normalizedStatus = "INACTIVE";
                }
            }

            log.info("✅ API VERIFIED: {} ({})", name, normalizedStatus);

            return new CompanyVerificationResponse(
                    true,
                    normalizedStatus,
                    registryUrl,
                    "Verified via OpenCorporates"
            );

        } catch (Exception e) {
            log.warn("❌ API failed: {}", e.getMessage());
            return null;
        }
    }

    // ===============================
    // 🌐 DNS FALLBACK METHOD
    // ===============================
    private CompanyVerificationResponse verifyViaDNS(String companyName) {

        try {
            // Clean company name
            String cleaned = companyName.toLowerCase()
                    .replaceAll("(pvt|ltd|limited|inc)", "")
                    .replaceAll("\\s+", "")
                    .replaceAll("[^a-z0-9]", "");

            if (cleaned.isEmpty()) {
                return new CompanyVerificationResponse(false, "UNKNOWN", null, "Invalid company name");
            }

            String[] domains = {
                    cleaned + ".com",
                    cleaned + ".org",
                    cleaned + ".net"
            };

            for (String domain : domains) {
                try {
                    InetAddress.getByName(domain);

                    log.info("✅ DNS FOUND: {}", domain);

                    return new CompanyVerificationResponse(
                            true,
                            "ACTIVE",
                            domain,
                            "Verified via DNS"
                    );

                } catch (Exception ignored) {}
            }

            log.warn("⚠️ No DNS found for {}", companyName);

            return new CompanyVerificationResponse(
                    false,
                    "UNKNOWN",
                    null,
                    "Domain not found"
            );

        } catch (Exception e) {
            return new CompanyVerificationResponse(false, "UNKNOWN", null, "DNS error");
        }
    }
}