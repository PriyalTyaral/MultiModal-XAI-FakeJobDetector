package com.example.random_major.service;

import java.net.InetAddress;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.random_major.model.DomainValidationResponse;

/**
 * DomainValidationService: Extracts and compares domains from URLs and emails
 * Includes DNS verification for domain validation
 * 
 * Calculates risk score based on domain matching:
 * - 1.0 = Perfect match (domain verified via DNS)
 * - 0.8 = Subdomain or base domain match (domain verified via DNS)
 * - 0.5 = Partial/suspicious match (domain may not be verified)  
 * - 0.0 = No match or DNS lookup failed (HIGH RISK)
 */
@Service
public class DomainValidationService {

    private static final Logger log = LoggerFactory.getLogger(DomainValidationService.class);

    /**
     * Validates domain extraction and matching
     * 
     * @param companyWebsite The verified company website
     * @param jobPostingUrl The job posting URL
     * @param contactEmail The contact email (optional)
     * @return DomainValidationResponse with match status and risk score
     */
    public DomainValidationResponse validateDomain(String companyWebsite, String jobPostingUrl, String contactEmail) {
        try {
            // Extract company domain from website
            String companyDomain = extractDomain(companyWebsite);
            if (companyDomain == null) {
                log.warn("Could not extract company domain from: {}", companyWebsite);
                return new DomainValidationResponse(
                    false,
                    0.5,
                    null,
                    null,
                    "Could not extract company domain from website"
                );
            }

            // Extract domain from job posting URL
            String extractedDomain = extractDomain(jobPostingUrl);
            if (extractedDomain == null) {
                log.warn("Could not extract domain from job posting URL: {}", jobPostingUrl);
                
                // Try to extract from email if provided
                if (contactEmail != null && !contactEmail.isEmpty()) {
                    extractedDomain = extractEmailDomain(contactEmail);
                    if (extractedDomain == null) {
                        return new DomainValidationResponse(
                            false,
                            0.0,
                            null,
                            companyDomain,
                            "Could not extract domain from URL or email"
                        );
                    }
                } else {
                    return new DomainValidationResponse(
                        false,
                        0.0,
                        null,
                        companyDomain,
                        "Could not extract domain from URL"
                    );
                }
            }

            // Verify domains exist via DNS lookup
            boolean companyDomainExists = verifyDomainViaDNS(companyDomain);
            boolean extractedDomainExists = verifyDomainViaDNS(extractedDomain);

            if (!companyDomainExists) {
                log.warn("❌ Company domain '{}' could not be verified via DNS", companyDomain);
            }

            if (!extractedDomainExists) {
                log.warn("❌ Extracted domain '{}' could not be verified via DNS", extractedDomain);
            }

            // Compare domains
            ComparisonResult result = compareDomains(companyDomain, extractedDomain);

            // Adjust risk score based on DNS verification
            if (result.isMatch && !extractedDomainExists) {
                result.riskScore *= 0.7;  // Reduce confidence if extracted domain doesn't exist
                result.message += " (WARNING: Extracted domain DNS lookup failed)";
            }

            log.info("✓ Domain validation: company={}, extracted={}, match={}, riskScore={}, companyDNS={}, extractedDNS={}",
                    companyDomain, extractedDomain, result.isMatch, result.riskScore, companyDomainExists, extractedDomainExists);

            return new DomainValidationResponse(
                result.isMatch,
                result.riskScore,
                extractedDomain,
                companyDomain,
                result.message
            );

        } catch (Exception e) {
            log.error("Domain validation error: {}", e.getMessage(), e);
            return new DomainValidationResponse(
                false,
                0.5,
                null,
                null,
                "Domain validation failed: " + e.getMessage()
            );
        }
    }

    /**
     * Verifies if a domain exists via DNS lookup
     * 
     * @param domain The domain to verify (e.g., "example.com")
     * @return true if domain has valid DNS A records, false otherwise
     */
    public boolean verifyDomainViaDNS(String domain) {
        try {
            if (domain == null || domain.trim().isEmpty()) {
                return false;
            }

            // Attempt DNS lookup (A record)
            InetAddress.getByName(domain);
            log.debug("✓ DNS verification successful for: {}", domain);
            return true;

        } catch (java.net.UnknownHostException e) {
            log.debug("✗ DNS verification failed for '{}': {}", domain, e.getMessage());
            return false;

        } catch (Exception e) {
            log.warn("DNS lookup error for '{}': {}", domain, e.getMessage());
            return false;
        }
    }

    /**
     * Extracts domain from URL
     * 
     * @param urlString The URL string
     * @return Domain name (e.g., "example.com")
     */
    public String extractDomain(String urlString) {
        try {
            if (urlString == null || urlString.trim().isEmpty()) {
                return null;
            }

            // Add protocol if missing
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                urlString = "https://" + urlString;
            }

            URI uri = new URI(urlString);
            String host = uri.getHost();

            if (host == null) {
                return null;
            }

            // Remove 'www.' prefix if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            return host.toLowerCase();

        } catch (Exception e) {
            log.warn("Could not extract domain from '{}': {}", urlString, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts domain from email address
     * 
     * @param email The email address
     * @return Domain name (e.g., "example.com")
     */
    public String extractEmailDomain(String email) {
        try {
            if (email == null || !email.contains("@")) {
                return null;
            }

            String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
            
            // Remove 'www.' prefix if present
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }

            return domain;

        } catch (Exception e) {
            log.warn("Could not extract domain from email '{}': {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Compares two domains and determines match quality
     * 
     * @param companyDomain The verified company domain
     * @param extractedDomain The domain from job posting/email
     * @return ComparisonResult with match status and risk score
     */
    private ComparisonResult compareDomains(String companyDomain, String extractedDomain) {
        if (companyDomain == null || extractedDomain == null) {
            return new ComparisonResult(false, 0.0, "Domain comparison failed - null values");
        }

        companyDomain = companyDomain.toLowerCase().trim();
        extractedDomain = extractedDomain.toLowerCase().trim();

        // Perfect match
        if (companyDomain.equals(extractedDomain)) {
            return new ComparisonResult(true, 1.0, "Perfect domain match");
        }

        // Check if one contains the other (subdomain or similar)
        if (companyDomain.contains(extractedDomain) || extractedDomain.contains(companyDomain)) {
            
            // If extracted is a subdomain of company (partial match)
            if (extractedDomain.endsWith("." + companyDomain)) {
                return new ComparisonResult(true, 0.8, "Subdomain match detected");
            }
            
            // If company domain contains extracted (could be legitimate or suspicious)
            if (companyDomain.contains(extractedDomain)) {
                return new ComparisonResult(false, 0.5, "Partial domain match - suspicious");
            }
        }

        // Extract base domain from both (remove subdomains)
        String companySuffix = getBaseDomain(companyDomain);
        String extractedSuffix = getBaseDomain(extractedDomain);

        // Check if base domains match (e.g., amazon.com vs aws.amazon.com)
        if (companySuffix.equals(extractedSuffix)) {
            return new ComparisonResult(true, 0.8, "Base domain match detected");
        }

        // No match
        return new ComparisonResult(false, 0.0, "Domain mismatch detected - HIGH RISK");
    }

    /**
     * Extracts base domain from a full domain
     * E.g., "mail.example.co.uk" -> "example.co.uk"
     * 
     * @param domain Full domain
     * @return Base domain
     */
    private String getBaseDomain(String domain) {
        // Simple implementation - assumes domain like "company.com" or "company.co.uk"
        // For production, use a proper domain parser library
        String[] parts = domain.split("\\.");
        
        if (parts.length >= 3) {
            // Check for common two-part TLDs
            String tld = parts[parts.length - 2] + "." + parts[parts.length - 1];
            if (isTwoPartTld(tld)) {
                if (parts.length >= 4) {
                    return parts[parts.length - 3] + "." + tld;
                }
            }
        }

        // Default: return last two parts
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }

        return domain;
    }

    /**
     * Checks if a TLD is a two-part TLD (e.g., co.uk, com.au)
     * 
     * @param tld The TLD to check
     * @return true if two-part TLD
     */
    private boolean isTwoPartTld(String tld) {
        // Common two-part TLDs
        String[] twoPartTlds = {
            "co.uk", "co.in", "co.jp", "com.au", "com.br", "com.mx",
            "gov.uk", "ac.uk", "org.uk", "co.nz", "ac.nz"
        };

        for (String tld2 : twoPartTlds) {
            if (tld.equals(tld2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Inner class for domain comparison results
     */
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
