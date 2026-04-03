package com.example.random_major.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.random_major.entity.ExtractedData;

@DisplayName("EntityExtractionService Tests")
class EntityExtractionServiceTest {

    private EntityExtractionService entityExtractionService;

    @BeforeEach
    void setUp() {
        entityExtractionService = new EntityExtractionService();
    }

    // ──────────────────────────────────────────────────────────────────
    // BASIC HAPPY PATH TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should extract company, URL, and domain from full text")
    void testFullExtraction() {
        String text = "Join our team at Infosys! We are hiring talented engineers. Apply here: https://infosys.com/careers";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Infosys", result.getCompanyName());
        assertEquals("https://infosys.com/careers", result.getUrl());
        assertEquals("infosys.com", result.getDomain());
    }

    @Test
    @DisplayName("Should extract company from domain when not found in text")
    void testCompanyExtractionFromDomain() {
        String text = "Check out this exciting opportunity at https://acme.com/jobs";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Acme", result.getCompanyName());
        assertEquals("https://acme.com/jobs", result.getUrl());
        assertEquals("acme.com", result.getDomain());
    }

    @Test
    @DisplayName("Should extract URL and domain without company name")
    void testExtractUrlWithoutCompanyName() {
        String text = "This is a text-only job posting. Visit https://example.com for more details.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertNull(result.getCompanyName());
        assertEquals("https://example.com", result.getUrl());
        assertEquals("example.com", result.getDomain());
    }

    // ──────────────────────────────────────────────────────────────────
    // URL EXTRACTION TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should extract HTTPS URL")
    void testExtractHttpsUrl() {
        String text = "Apply at https://company.com/apply";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("https://company.com/apply", result.getUrl());
    }

    @Test
    @DisplayName("Should extract HTTP URL")
    void testExtractHttpUrl() {
        String text = "Visit http://example.org for the job posting";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("http://example.org", result.getUrl());
    }

    @Test
    @DisplayName("Should extract FTP URL")
    void testExtractFtpUrl() {
        String text = "Download details from ftp://files.company.net/jobs.txt";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("ftp://files.company.net/jobs.txt", result.getUrl());
    }

    @Test
    @DisplayName("Should extract first URL when multiple URLs present")
    void testExtractFirstUrlFromMultiple() {
        String text = "First: https://first.com Second: https://second.com";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("https://first.com", result.getUrl());
    }

    @Test
    @DisplayName("Should return null URL when no URL found")
    void testNoUrlFound() {
        String text = "This is a text-only job posting with no URLs at all";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertNull(result.getUrl());
        assertNull(result.getDomain());
    }

    // ──────────────────────────────────────────────────────────────────
    // DOMAIN EXTRACTION TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should extract domain from simple URL")
    void testDomainExtraction() {
        String text = "Visit https://example.com/page";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("example.com", result.getDomain());
    }

    @Test
    @DisplayName("Should remove www from domain")
    void testRemoveWwwFromDomain() {
        String text = "Go to https://www.google.com/search";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("google.com", result.getDomain());
    }

    @Test
    @DisplayName("Should extract domain with subdomain")
    void testSubdomainInUrl() {
        String text = "Check https://jobs.company.com/careers";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("company.com", result.getDomain());
    }

    // ──────────────────────────────────────────────────────────────────
    // COMPANY NAME EXTRACTION TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should extract company using 'Company: XYZ' pattern")
    void testCompanyFromCompanyPattern() {
        String text = "Company: Microsoft is hiring software engineers";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Microsoft", result.getCompanyName());
    }

    @Test
    @DisplayName("Should extract company using 'at XYZ' pattern")
    void testCompanyFromAtPattern() {
        String text = "We are hiring at Google. Check our careers page.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Google", result.getCompanyName());
    }

    @Test
    @DisplayName("Should extract company using 'join XYZ' pattern")
    void testCompanyFromJoinPattern() {
        String text = "Join Amazon and work with the best engineers in the world.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Amazon", result.getCompanyName());
    }

    @Test
    @DisplayName("Should extract company using 'hiring at XYZ' pattern")
    void testCompanyFromHiringPattern() {
        String text = "We are hiring at Apple for multiple positions.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Apple", result.getCompanyName());
    }

    @Test
    @DisplayName("Should be case-insensitive for patterns")
    void testCaseInsensitivePatterns() {
        String text = "JoIn ORACLE for an exciting role";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("ORACLE", result.getCompanyName());
    }

    // ──────────────────────────────────────────────────────────────────
    // COMPANY NAME CLEANING TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should remove 'Ltd' from company name")
    void testRemoveLtd() {
        String text = "Company: Acme Ltd. We are hiring.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Acme", result.getCompanyName());
    }

    @Test
    @DisplayName("Should remove 'Pvt' from company name")
    void testRemovePvt() {
        String text = "Join Tata Pvt Limited for this role.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Tata", result.getCompanyName());
    }

    @Test
    @DisplayName("Should remove 'Inc' from company name")
    void testRemoveInc() {
        String text = "At Intel Inc. we value innovation.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Intel", result.getCompanyName());
    }

    @Test
    @DisplayName("Should remove 'LLC' from company name")
    void testRemoveLLC() {
        String text = "Join Acme LLC for a great opportunity.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Acme", result.getCompanyName());
    }

    @Test
    @DisplayName("Should remove 'Corporation' from company name")
    void testRemoveCorporation() {
        String text = "At Microsoft Corporation, we're hiring engineers.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Microsoft", result.getCompanyName());
    }

    @Test
    @DisplayName("Should clean multiple cleanup words")
    void testCleanMultipleWords() {
        String text = "Company: Acme Pvt Ltd Inc. We are hiring.";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Acme", result.getCompanyName());
    }

    @Test
    @DisplayName("Should trim extra spaces")
    void testTrimSpaces() {
        String text = "Company:   Acme   Technologies  Inc   ";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Acme Technologies", result.getCompanyName());
    }

    // ──────────────────────────────────────────────────────────────────
    // EDGE CASE TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should handle null text")
    void testNullText() {
        ExtractedData result = entityExtractionService.extractFromText(null);
        
        assertNull(result.getCompanyName());
        assertNull(result.getUrl());
        assertNull(result.getDomain());
    }

    @Test
    @DisplayName("Should handle empty text")
    void testEmptyText() {
        ExtractedData result = entityExtractionService.extractFromText("");
        
        assertNull(result.getCompanyName());
        assertNull(result.getUrl());
        assertNull(result.getDomain());
    }

    @Test
    @DisplayName("Should handle whitespace-only text")
    void testWhitespaceOnlyText() {
        ExtractedData result = entityExtractionService.extractFromText("   \n\t   ");
        
        assertNull(result.getCompanyName());
        assertNull(result.getUrl());
        assertNull(result.getDomain());
    }

    @Test
    @DisplayName("Should handle text with no structured data")
    void testTextWithNoStructuredData() {
        String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertNull(result.getCompanyName());
        assertNull(result.getUrl());
        assertNull(result.getDomain());
    }

    @Test
    @DisplayName("Should handle invalid URL gracefully")
    void testInvalidUrlHandling() {
        String text = "Invalid URL: [not a valid url]";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertNull(result.getUrl());
    }

    @Test
    @DisplayName("Should extract company from domain with numbers")
    void testDomainWithNumbers() {
        String text = "Visit https://company123.com/careers";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Company123", result.getCompanyName());
        assertEquals("company123.com", result.getDomain());
    }

    @Test
    @DisplayName("Should handle URLs with complex paths")
    void testUrlWithComplexPath() {
        String text = "Apply at https://example.com/en/careers/jobs?id=123&region=US";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertNotNull(result.getUrl());
        assertEquals("example.com", result.getDomain());
    }

    // ──────────────────────────────────────────────────────────────────
    // REAL-WORLD SCENARIO TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should handle LinkedIn job posting format")
    void testLinkedInFormat() {
        String text = """
            Join our team at Infosys!
            We are looking for Senior Software Engineers.
            
            Infosys Limited is a global leader in next-generation digital services.
            Apply now: https://infosys.com/careers/senior-engineer
            """;
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Infosys", result.getCompanyName());
        assertEquals("https://infosys.com/careers/senior-engineer", result.getUrl());
        assertEquals("infosys.com", result.getDomain());
    }

    @Test
    @DisplayName("Should handle Indeed job posting format")
    void testIndeedFormat() {
        String text = """
            XYZ Corporation Pvt Ltd
            Job Title: Software Developer
            Location: New York, NY
            
            We are actively hiring at XYZ Corporation
            Visit our careers page: https://www.xyzcorp.com/jobs
            """;
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("XYZ", result.getCompanyName());
        assertEquals("https://www.xyzcorp.com/jobs", result.getUrl());
        assertEquals("xyzcorp.com", result.getDomain());
    }

    @Test
    @DisplayName("Should handle minimal text with only URL and domain")
    void testMinimalText() {
        String text = "https://techcompany.io";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertEquals("Techcompany", result.getCompanyName()); // Extracted from domain
        assertEquals("https://techcompany.io", result.getUrl());
        assertEquals("techcompany.io", result.getDomain());
    }

    // ──────────────────────────────────────────────────────────────────
    // DATA STRUCTURE TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ExtractedData should have all three fields")
    void testExtractedDataStructure() {
        String text = "Join Acme at https://acme.com/careers";
        
        ExtractedData result = entityExtractionService.extractFromText(text);
        
        assertNotNull(result);
        assertNotNull(result.getCompanyName());
        assertNotNull(result.getUrl());
        assertNotNull(result.getDomain());
    }

    @Test
    @DisplayName("ExtractedData string representation should be formatted correctly")
    void testExtractedDataToString() {
        ExtractedData data = new ExtractedData("TestCorp", "https://test.com", "test.com");
        String toString = data.toString();
        
        assertTrue(toString.contains("TestCorp"));
        assertTrue(toString.contains("https://test.com"));
        assertTrue(toString.contains("test.com"));
    }
}
