package com.example.random_major.service;

import java.net.InetAddress;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.random_major.model.DomainValidationResponse;

@Service
public class DomainValidationService {

    private static final Logger log = LoggerFactory.getLogger(DomainValidationService.class);

    public DomainValidationResponse validateDomain(String companyWebsite, String jobPostingUrl, String contactEmail) {
        try {

            String companyDomain = extractDomain(companyWebsite);
            String extractedDomain = extractDomain(jobPostingUrl);

            // Fallback to email domain
            if (extractedDomain == null && contactEmail != null) {
                extractedDomain = extractEmailDomain(contactEmail);
            }

            if (companyDomain == null || extractedDomain == null) {
                return new DomainValidationResponse(false, 0.0, extractedDomain, companyDomain,
                        "Domain extraction failed");
            }

            // 🚨 Free email detection (HIGH RISK)
            if (contactEmail != null) {
                String emailDomain = extractEmailDomain(contactEmail);
                if (isFreeEmail(emailDomain)) {
                    return new DomainValidationResponse(false, 0.2, emailDomain, companyDomain,
                            "Free email domain used (gmail/yahoo) - HIGH RISK");
                }
            }

            boolean companyDNS = verifyDomainViaDNS(companyDomain);
            boolean extractedDNS = verifyDomainViaDNS(extractedDomain);

            ComparisonResult result = compareDomains(companyDomain, extractedDomain);

            // 🚨 DNS-based adjustments
            if (!extractedDNS) {
                result.riskScore *= 0.5;
                result.message += " (Domain does not exist)";
            }

            if (extractedDNS && !result.isMatch) {
                result.riskScore *= 0.5;
                result.message += " (Domain exists but mismatch)";
            }

            log.info("✓ Domain validation: company={}, extracted={}, match={}, score={}",
                    companyDomain, extractedDomain, result.isMatch, result.riskScore);

            return new DomainValidationResponse(
                    result.isMatch,
                    result.riskScore,
                    extractedDomain,
                    companyDomain,
                    result.message
            );

        } catch (Exception e) {
            log.error("Domain validation error: {}", e.getMessage());
            return new DomainValidationResponse(false, 0.5, null, null, "Validation failed");
        }
    }

    // =========================
    // 🌐 DNS CHECK
    // =========================
    public boolean verifyDomainViaDNS(String domain) {
        try {
            InetAddress.getByName(domain);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // 🔗 DOMAIN EXTRACTION
    // =========================
    public String extractDomain(String url) {
        try {
            if (url == null || url.isEmpty()) return null;

            if (!url.startsWith("http")) {
                url = "https://" + url;
            }

            URI uri = new URI(url);
            String host = uri.getHost();

            if (host == null) return null;

            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            return host.toLowerCase();

        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // 📧 EMAIL DOMAIN
    // =========================
    public String extractEmailDomain(String email) {
        try {
            if (email == null || !email.contains("@")) return null;
            return email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // 🚨 FREE EMAIL CHECK
    // =========================
    private boolean isFreeEmail(String domain) {
        String[] free = {
                "gmail.com", "yahoo.com", "outlook.com", "hotmail.com"
        };

        for (String f : free) {
            if (f.equals(domain)) return true;
        }
        return false;
    }

    // =========================
    // 🧠 DOMAIN COMPARISON
    // =========================
    private ComparisonResult compareDomains(String companyDomain, String extractedDomain) {

        // Exact match
        if (companyDomain.equals(extractedDomain)) {
            return new ComparisonResult(true, 1.0, "Perfect match");
        }

        // Subdomain match
        if (extractedDomain.endsWith("." + companyDomain)) {
            return new ComparisonResult(true, 0.8, "Subdomain match");
        }

        // Base domain match
        String base1 = getBaseDomain(companyDomain);
        String base2 = getBaseDomain(extractedDomain);

        if (base1.equals(base2)) {
            return new ComparisonResult(true, 0.7, "Base domain match");
        }

        // 🚨 Domain spoofing detection
        if (extractedDomain.contains(companyDomain) && !extractedDomain.endsWith(companyDomain)) {
            return new ComparisonResult(false, 0.2, "Domain spoofing detected");
        }

        // No match
        return new ComparisonResult(false, 0.0, "Domain mismatch - HIGH RISK");
    }

    // =========================
    // 🌍 BASE DOMAIN
    // =========================
    private String getBaseDomain(String domain) {
        String[] parts = domain.split("\\.");

        if (parts.length >= 2) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }

        return domain;
    }

    // =========================
    // INNER CLASS
    // =========================
    private static class ComparisonResult {
        boolean isMatch;
        double riskScore;
        String message;

        ComparisonResult(boolean isMatch, double riskScore, String message) {
            this.isMatch = isMatch;
            this.riskScore = riskScore;
            this.message = message;
        }
    }
}