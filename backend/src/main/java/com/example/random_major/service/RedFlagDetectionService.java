package com.example.random_major.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.random_major.model.RedFlag;

/**
 * ENHANCED RedFlagDetectionService: Advanced multi-category fraud detection
 * 
 * Detects sophisticated fraud patterns in job postings across 4 categories:
 * A) Financial & Economic Red Flags (Skill/Pay Mismatch, Equipment Scam, Upfront Payments, Vague Benefits)
 * B) Communication & Platform Red Flags (Non-Corporate Channels, Email Domain Spoofing, Phishing)
 * C) Structural & Technical Red Flags (Domain Age, Hyphenated Domains, Broken Branding)
 * D) Behavioral & Contextual Red Flags (Urgency, Process Inconsistency, Global/Local Mismatch, Social Proof Gap)
 * 
 * Each flag has:
 * - Type & Category
 * - Weight (0.1 to 0.6)
 * - Description & Evidence
 * 
 * Final Score = sum(weights) / maxPossible, capped at 1.0
 */
@Service
public class RedFlagDetectionService {

    private static final Logger log = LoggerFactory.getLogger(RedFlagDetectionService.class);

    // ═══════════════════════════════════════════════════════════════════
    // WEIGHT CONSTANTS: Financial & Economic Red Flags
    // ═══════════════════════════════════════════════════════════════════
    private static final double WEIGHT_SKILL_PAY_MISMATCH = 0.5;     // HIGH
    private static final double WEIGHT_EQUIPMENT_CHECK_SCAM = 0.5;   // HIGH
    private static final double WEIGHT_UPFRONT_PAYMENT = 0.6;        // VERY HIGH
    private static final double WEIGHT_VAGUE_BENEFITS = 0.2;         // LOW/MEDIUM

    // ═══════════════════════════════════════════════════════════════════
    // WEIGHT CONSTANTS: Communication & Platform Red Flags
    // ═══════════════════════════════════════════════════════════════════
    private static final double WEIGHT_NON_CORPORATE_CHANNEL = 0.4;  // HIGH
    private static final double WEIGHT_EMAIL_DOMAIN_MISMATCH = 0.5;  // HIGH
    private static final double WEIGHT_DOMAIN_SPOOFING = 0.55;       // HIGH
    private static final double WEIGHT_PHISHING_GATE = 0.55;         // HIGH

    // ═══════════════════════════════════════════════════════════════════
    // WEIGHT CONSTANTS: Structural & Technical Red Flags
    // ═══════════════════════════════════════════════════════════════════
    private static final double WEIGHT_HYPHENATED_DOMAIN = 0.4;      // MEDIUM/HIGH
    private static final double WEIGHT_DOMAIN_MISMATCH = 0.45;       // MEDIUM/HIGH
    private static final double WEIGHT_AI_PERFECTION = 0.15;         // LOW

    // ═══════════════════════════════════════════════════════════════════
    // WEIGHT CONSTANTS: Behavioral & Contextual Red Flags
    // ═══════════════════════════════════════════════════════════════════
    private static final double WEIGHT_EXTREME_URGENCY = 0.3;        // MEDIUM
    private static final double WEIGHT_PROCESS_INCONSISTENCY = 0.35; // MEDIUM
    private static final double WEIGHT_GLOBAL_LOCAL_MISMATCH = 0.4;  // MEDIUM/HIGH
    private static final double WEIGHT_SOCIAL_PROOF_GAP = 0.25;      // LOW/MEDIUM

    // ═══════════════════════════════════════════════════════════════════
    // WEIGHT CONSTANTS: Legacy (for backward compatibility)
    // ═══════════════════════════════════════════════════════════════════
    private static final double WEIGHT_HIGH_SALARY = 0.2;

    // ═══════════════════════════════════════════════════════════════════
    // PATTERN CONSTANTS: Financial & Economic
    // ═══════════════════════════════════════════════════════════════════
    // Entry-level jobs with extremely high pay
    private static final Pattern ENTRY_LEVEL_PATTERN = Pattern.compile(
        "\\b(data entry|typing|no experience|fresher|beginner|entry.?level)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // High pay indicators
    private static final Pattern HIGH_PAY_PATTERN = Pattern.compile(
        "\\$30[0-9]?k?|₹30,?000|€30k|\\$30.hour|daily payout|free money",
        Pattern.CASE_INSENSITIVE
    );

    // Equipment check scam - Break into two patterns for +30% boost detection
    private static final Pattern EQUIPMENT_CHECK_PATTERN = Pattern.compile(
        "\\b(company check|equipment stipend|send you a check|we will provide|check arriving|equipment provided)\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    // Check/Stipend keywords (first condition for 30% boost)
    private static final Pattern CHECK_STIPEND_PATTERN = Pattern.compile(
        "\\b(check|stipend|reimbursement|provide)\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    // Equipment/Hardware keywords (second condition for 30% boost)
    private static final Pattern EQUIPMENT_HARDWARE_PATTERN = Pattern.compile(
        "\\b(equipment|hardware|laptop|computer|phone|device|tools|device cost)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Upfront payment requests
    private static final Pattern UPFRONT_PAYMENT_PATTERN = Pattern.compile(
        "\\b(registration fee|training fee|processing fee|visa fee|application fee|membership fee|upfront|advance payment|deposit required)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Vague benefits
    private static final Pattern VAGUE_BENEFITS_PATTERN = Pattern.compile(
        "\\b(work from bed|daily payout|easy money|flexible work anytime|no experience required|work whenever|unlimited income|passive income)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // ═══════════════════════════════════════════════════════════════════
    // PATTERN CONSTANTS: Communication & Platform
    // ═══════════════════════════════════════════════════════════════════
    // Non-corporate messaging channels
    private static final Pattern NON_CORPORATE_CHANNEL_PATTERN = Pattern.compile(
        "\\b(telegram|@[a-z0-9_]{5,}|whatsapp|signal|discord|viber|wechat|icq)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Negation modifiers - for flipping RED flags to GREEN (sign of real post)
    private static final Pattern NEGATION_MODIFIER_PATTERN = Pattern.compile(
        "\\b(no|not|never|avoid|don't|don't use|don't contact|no need|no telegram|no whatsapp)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Phishing login gates
    private static final Pattern PHISHING_GATE_PATTERN = Pattern.compile(
        "\\b(login with|sign in to|authenticate|create account to apply|download the app|install extension)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // ═══════════════════════════════════════════════════════════════════
    // PATTERN CONSTANTS: Structural & Technical
    // ═══════════════════════════════════════════════════════════════════
    // Domain spoofing indicators
    private static final Pattern DOMAIN_SPOOFING_PATTERN = Pattern.compile(
        "\\b(apple|google|amazon|microsoft|facebook|meta|tesla|nvidia|ibm|oracle|infosys|tcs)-[a-z0-9-]+\\.(xyz|net|co|icu|top|ml|tk)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // AI perfection markers
    private static final Pattern AI_PERFECTION_PATTERN = Pattern.compile(
        "\\b(in conclusion|furthermore|moreover|to summarize|sincerely|regards|best regards)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // ═══════════════════════════════════════════════════════════════════
    // PATTERN CONSTANTS: Behavioral & Contextual
    // ═══════════════════════════════════════════════════════════════════
    // Extreme urgency
    private static final Pattern EXTREME_URGENCY_PATTERN = Pattern.compile(
        "\\b(immediate joining|last few seats|limited positions|apply now|urgent hiring|final day|ends today)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Interview process markers
    private static final Pattern INTERVIEW_MARKER_PATTERN = Pattern.compile(
        "\\b(interview|round|technical test|coding challenge|assessment|screening|discussion)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Global/Local mismatch
    private static final Pattern GLOBAL_LOCATION_PATTERN = Pattern.compile(
        "\\b(london|uk|usa|us|new york|california|remote|work from home)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LOCAL_BENEFIT_PATTERN = Pattern.compile(
        "\\b(w2|1099|401k|ira|visa sponsorship|h1b)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Email extraction pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );

    // Salary threshold: 150% of median US salary (~$60k/year)
    private static final double SALARY_THRESHOLD = 90000;

    // ═══════════════════════════════════════════════════════════════════
    // MAIN DETECTION METHOD (ENHANCED)
    // ═══════════════════════════════════════════════════════════════════
    /**
     * Detect all red flags in a job posting across all 4 categories
     * 
     * @param jobText The job posting text
     * @return List of detected red flags with enhanced categorization
     */
    public List<RedFlag> detectRedFlags(String jobText) {
        List<RedFlag> flags = new ArrayList<>();

        if (jobText == null || jobText.trim().isEmpty()) {
            return flags;
        }

        String textLower = jobText.toLowerCase();
        
        log.info("🚩 ENHANCED RED FLAG DETECTION STARTED...");

        // ─────────────────────────────────────────────────────────
        // CATEGORY A: Financial & Economic Red Flags
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Financial & Economic red flags...");
        detectSkillPayMismatch(textLower, flags);
        detectEquipmentCheckScam(textLower, flags);
        detectUpfrontPayment(textLower, flags);
        detectVagueBenefits(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // CATEGORY B: Communication & Platform Red Flags
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Communication & Platform red flags...");
        detectNonCorporateChannel(textLower, flags);
        detectEmailDomainMismatch(textLower, flags);
        detectDomainSpoofing(textLower, flags);
        detectPhishingGate(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // CATEGORY C: Structural & Technical Red Flags
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Structural & Technical red flags...");
        detectHyphenatedDomain(textLower, flags);
        detectDomainMismatch(textLower, flags);
        detectAIPerfection(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // CATEGORY D: Behavioral & Contextual Red Flags
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Behavioral & Contextual red flags...");
        detectExtremeUrgency(textLower, flags);
        detectProcessInconsistency(textLower, flags);
        detectGlobalLocalMismatch(textLower, flags);
        detectSocialProofGap(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // LEGACY COMPATIBILITY: Keep basic detection
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking legacy patterns...");
        detectHighSalary(jobText, flags);

        log.info("✅ RED FLAG DETECTION COMPLETED: {} flags detected", flags.size());
        for (RedFlag flag : flags) {
            log.info("   • [{}] {} (weight: {}, evidence: {})", 
                flag.getType(), flag.getDescription(), flag.getWeight(), flag.getEvidence());
        }

        return flags;
    }

    /**
     * ENHANCED: Detect red flags with company verification context
     * 
     * This overloaded method accepts company verification data to provide context-aware weighting.
     * For example, strong company verification lowers PROCESS_INCONSISTENCY weight from 0.35 to 0.05-0.10
     * because legitimate high-level companies may skip interview details to keep posts concise.
     * 
     * @param jobText The job posting text
     * @param companyName The company name (for context)
     * @param isCompanyVerified Whether the company is verified/legitimate
     * @return List of detected red flags with context-aware weighting
     */
    public List<RedFlag> detectRedFlags(String jobText, String companyName, boolean isCompanyVerified) {
        List<RedFlag> flags = new ArrayList<>();

        if (jobText == null || jobText.trim().isEmpty()) {
            return flags;
        }

        String textLower = jobText.toLowerCase();
        
        log.info("🚩 ENHANCED RED FLAG DETECTION STARTED (with company context)...");

        // ─────────────────────────────────────────────────────────
        // CATEGORY A: Financial & Economic Red Flags
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Financial & Economic red flags...");
        detectSkillPayMismatch(textLower, flags);
        detectEquipmentCheckScam(textLower, flags);
        detectUpfrontPayment(textLower, flags);
        detectVagueBenefits(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // CATEGORY B: Communication & Platform Red Flags
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Communication & Platform red flags...");
        detectNonCorporateChannel(textLower, flags);
        detectEmailDomainMismatch(textLower, flags);
        detectDomainSpoofing(textLower, flags);
        detectPhishingGate(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // CATEGORY C: Structural & Technical Red Flags
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Structural & Technical red flags...");
        detectHyphenatedDomain(textLower, flags);
        detectDomainMismatch(textLower, flags);
        detectAIPerfection(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // CATEGORY D: Behavioral & Contextual Red Flags (with company context)
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking Behavioral & Contextual red flags (with company context)...");
        detectExtremeUrgency(textLower, flags);
        detectProcessInconsistency(textLower, flags, isCompanyVerified);  // Pass company context
        detectGlobalLocalMismatch(textLower, flags);
        detectSocialProofGap(textLower, flags);

        // ─────────────────────────────────────────────────────────
        // LEGACY COMPATIBILITY: Keep basic detection
        // ─────────────────────────────────────────────────────────
        log.debug("→ Checking legacy patterns...");
        detectHighSalary(jobText, flags);

        log.info("✅ RED FLAG DETECTION COMPLETED (ENHANCED): {} flags detected", flags.size());
        for (RedFlag flag : flags) {
            log.info("   • [{}] {} (weight: {}, evidence: {})", 
                flag.getType(), flag.getDescription(), flag.getWeight(), flag.getEvidence());
        }

        return flags;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CATEGORY A: FINANCIAL & ECONOMIC RED FLAG DETECTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * A1: Detect Skill/Pay Mismatch
     * Combines entry-level indicators with high salary
     */
    private void detectSkillPayMismatch(String textLower, List<RedFlag> flags) {
        boolean hasEntryLevel = ENTRY_LEVEL_PATTERN.matcher(textLower).find();
        boolean hasHighPay = HIGH_PAY_PATTERN.matcher(textLower).find();

        if (hasEntryLevel && hasHighPay) {
            String evidence = concatEvidence(
                extractEvidence(textLower, ENTRY_LEVEL_PATTERN),
                extractEvidence(textLower, HIGH_PAY_PATTERN)
            );
            flags.add(new RedFlag(
                "SKILL_PAY_MISMATCH",
                WEIGHT_SKILL_PAY_MISMATCH,
                "[Financial] Suspicious skill-to-pay mismatch: Entry-level role with extremely high salary",
                evidence
            ));
            log.debug("   ⚠️  SKILL_PAY_MISMATCH detected");
        }
    }

    /**
     * A2: Detect Equipment Check Scam (Enhanced with +30% Boost)
     * 
     * Detection Rule:
     * - Base: Check/Stipend + Equipment/Hardware mentions = 0.5 weight
     * - Enhanced: BOTH ("Check" OR "Stipend") AND ("Equipment" OR "Hardware") = 0.8 weight (+30% boost)
     * 
     * Examples:
     * - "We will provide equipment" → 0.5 (base)
     * - "We will send you a check for equipment" → 0.8 (BOTH conditions met - add 30%)
     * - "Equipment stipend provided" → 0.8 (BOTH conditions met)
     */
    private void detectEquipmentCheckScam(String textLower, List<RedFlag> flags) {
        boolean hasCheckOrStipend = CHECK_STIPEND_PATTERN.matcher(textLower).find();
        boolean hasEquipmentOrHardware = EQUIPMENT_HARDWARE_PATTERN.matcher(textLower).find();
        
        if (hasCheckOrStipend && hasEquipmentOrHardware) {
            // BOTH conditions met → +30% boost (0.5 + 0.3 = 0.8)
            String evidence = concatEvidence(
                "Check/Stipend detected: " + extractEvidence(textLower, CHECK_STIPEND_PATTERN),
                "Equipment/Hardware detected: " + extractEvidence(textLower, EQUIPMENT_HARDWARE_PATTERN)
            );
            flags.add(new RedFlag(
                "EQUIPMENT_CHECK_SCAM",
                0.8,  // ENHANCED weight: 0.5 + 0.3 (30% boost)
                "[Financial] EQUIPMENT CHECK SCAM (+30% boost): BOTH Check/Stipend AND Equipment/Hardware keywords detected",
                evidence
            ));
            log.debug("   🚨 EQUIPMENT_CHECK_SCAM detected with +30% boost (weight: 0.8)");
        } else if (EQUIPMENT_CHECK_PATTERN.matcher(textLower).find()) {
            // Base detection - single pattern match
            flags.add(new RedFlag(
                "EQUIPMENT_CHECK_SCAM",
                WEIGHT_EQUIPMENT_CHECK_SCAM,
                "[Financial] Equipment check scam indicator: Mentions sending checks or equipment stipends",
                extractEvidence(textLower, EQUIPMENT_CHECK_PATTERN)
            ));
            log.debug("   ⚠️  EQUIPMENT_CHECK_SCAM detected (weight: 0.5)");
        }
    }

    /**
     * A3: Detect Upfront Payment Request
     * Registration, training, processing, visa fees
     */
    private void detectUpfrontPayment(String textLower, List<RedFlag> flags) {
        if (UPFRONT_PAYMENT_PATTERN.matcher(textLower).find()) {
            flags.add(new RedFlag(
                "UPFRONT_PAYMENT",
                WEIGHT_UPFRONT_PAYMENT,
                "[Financial] VERY HIGH RISK - Upfront payment required (fee, registration, training, visa)",
                extractEvidence(textLower, UPFRONT_PAYMENT_PATTERN)
            ));
            log.debug("   🚨 UPFRONT_PAYMENT detected");
        }
    }

    /**
     * A4: Detect Vague/Unrealistic Benefits
     * "Work from bed", "daily payout", "easy money", etc.
     */
    private void detectVagueBenefits(String textLower, List<RedFlag> flags) {
        if (VAGUE_BENEFITS_PATTERN.matcher(textLower).find()) {
            flags.add(new RedFlag(
                "VAGUE_BENEFITS",
                WEIGHT_VAGUE_BENEFITS,
                "[Financial] Vague or unrealistic benefits (no experience needed, work anytime, easy money)",
                extractEvidence(textLower, VAGUE_BENEFITS_PATTERN)
            ));
            log.debug("   ⚠️  VAGUE_BENEFITS detected");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CATEGORY B: COMMUNICATION & PLATFORM RED FLAG DETECTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * B1: Detect Non-Corporate Communication Channels (with Negation Handler)
     * Telegram, WhatsApp, Discord, Signal, etc.
     * 
     * NEGATION HANDLER:
     * - If channel mention has negation within 3 words → GREEN flag (sign of real post!)
     * - Example: "Do NOT contact via Telegram" → GREEN (legitimate warning)
     * - Example: "Contact on Telegram" → RED (fraud indicator)
     */
    private void detectNonCorporateChannel(String textLower, List<RedFlag> flags) {
        Matcher channelMatcher = NON_CORPORATE_CHANNEL_PATTERN.matcher(textLower);
        
        if (channelMatcher.find()) {
            String channelMatch = channelMatcher.group();
            int channelPos = channelMatcher.start();
            
            // Check for negation modifiers within 3 words before or after the channel mention
            if (hasNegationModifierNearby(textLower, channelPos, 3)) {
                // NEGATION DETECTED: This is a GREEN flag (sign of a REAL post!)
                flags.add(new RedFlag(
                    "NON_CORPORATE_CHANNEL_NEGATED",
                    -0.4,  // NEGATIVE weight (LOWERS fake score - sign of legitimate post!)
                    "[Communication] ✅ NEGATION HANDLER: Legitimate warning against using non-corporate channels (e.g., 'Do NOT contact via Telegram')",
                    "Negation detected near: " + channelMatch
                ));
                log.debug("   ✅ NON_CORPORATE_CHANNEL NEGATED (flipped to GREEN - legitimate post!)");
            } else {
                // NO NEGATION: Standard RED flag
                flags.add(new RedFlag(
                    "NON_CORPORATE_CHANNEL",
                    WEIGHT_NON_CORPORATE_CHANNEL,
                    "[Communication] Non-corporate communication channel: Telegram, WhatsApp, Discord, Signal detected",
                    extractEvidence(textLower, NON_CORPORATE_CHANNEL_PATTERN)
                ));
                log.debug("   ⚠️  NON_CORPORATE_CHANNEL detected (RED flag)");
            }
        }
    }

    /**
     * B2: Detect Email Domain Mismatch
     * Company name vs. email domain (e.g., "Infosys" with "@gmail.com")
     */
    private void detectEmailDomainMismatch(String textLower, List<RedFlag> flags) {
        // Extract emails from text
        Matcher emailMatcher = EMAIL_PATTERN.matcher(textLower);
        while (emailMatcher.find()) {
            String email = emailMatcher.group();
            String emailDomain = extractDomainFromEmail(email);
            
            // Check if it's a generic free email (gmail, yahoo, outlook, etc.)
            if (isGenericEmailDomain(emailDomain)) {
                flags.add(new RedFlag(
                    "EMAIL_DOMAIN_MISMATCH",
                    WEIGHT_EMAIL_DOMAIN_MISMATCH,
                    "[Communication] Email domain mismatch: Company contact uses free/generic email (gmail, yahoo, etc.)",
                    "Email: " + email
                ));
                log.debug("   ⚠️  EMAIL_DOMAIN_MISMATCH detected: {}", email);
                return; // Only add once
            }
        }
    }

    /**
     * B3: Detect Domain Spoofing
     * Fake company domains (e.g., "google-job-portal.net", "apple-support-jobs.xyz")
     */
    private void detectDomainSpoofing(String textLower, List<RedFlag> flags) {
        if (DOMAIN_SPOOFING_PATTERN.matcher(textLower).find()) {
            flags.add(new RedFlag(
                "DOMAIN_SPOOFING",
                WEIGHT_DOMAIN_SPOOFING,
                "[Communication] Domain spoofing detected: Fake company domains (e.g., google-job-portal.xyz)",
                extractEvidence(textLower, DOMAIN_SPOOFING_PATTERN)
            ));
            log.debug("   🚨 DOMAIN_SPOOFING detected");
        }
    }

    /**
     * B4: Detect Phishing Login Gates
     * "Login with Google", "Sign in to view", etc.
     */
    private void detectPhishingGate(String textLower, List<RedFlag> flags) {
        if (PHISHING_GATE_PATTERN.matcher(textLower).find()) {
            flags.add(new RedFlag(
                "PHISHING_GATE",
                WEIGHT_PHISHING_GATE,
                "[Communication] Phishing gate detected: Requires login/authentication to apply or view details",
                extractEvidence(textLower, PHISHING_GATE_PATTERN)
            ));
            log.debug("   🚨 PHISHING_GATE detected");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CATEGORY C: STRUCTURAL & TECHNICAL RED FLAG DETECTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * C1: Detect Hyphenated Domains
     * Multiple hyphens in domain (e.g., "apple-support-jobs-portal.com")
     */
    private void detectHyphenatedDomain(String textLower, List<RedFlag> flags) {
        // Count hyphens in URLs
        Pattern urlPattern = Pattern.compile("https?://([a-z0-9-]+\\.[a-z.]+)", Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = urlPattern.matcher(textLower);
        
        while (urlMatcher.find()) {
            String domain = urlMatcher.group(1);
            long hyphenCount = domain.chars().filter(ch -> ch == '-').count();
            
            if (hyphenCount >= 2) {
                flags.add(new RedFlag(
                    "HYPHENATED_DOMAIN",
                    WEIGHT_HYPHENATED_DOMAIN,
                    "[Technical] Hyphenated domain detected: Multiple hyphens suggest spoofing",
                    "Domain: " + domain + " (hyphens: " + hyphenCount + ")"
                ));
                log.debug("   ⚠️  HYPHENATED_DOMAIN detected: {}", domain);
            }
        }
    }

    /**
     * C2: Detect Domain Mismatch
     * Domain differs from company name
     */
    private void detectDomainMismatch(String textLower, List<RedFlag> flags) {
        Pattern urlPattern = Pattern.compile("https?://([a-z0-9-]+\\.[a-z.]+)", Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = urlPattern.matcher(textLower);
        
        if (urlMatcher.find()) {
            String domain = urlMatcher.group(1);
            // Extract company name patterns like "Company Name:" or "Hiring from:"
            Pattern companyPattern = Pattern.compile("company:?\\s+([A-Za-z\\s&]+?)(?:[,\\n]|$)", Pattern.CASE_INSENSITIVE);
            Matcher companyMatcher = companyPattern.matcher(textLower);
            
            if (companyMatcher.find()) {
                String company = companyMatcher.group(1).trim();
                String domainPrefix = domain.split("\\.")[0].replace("-", "");
                String companyFormatted = company.toLowerCase().replaceAll("\\s+", "");
                
                if (!domainPrefix.contains(companyFormatted) && !companyFormatted.contains(domainPrefix)) {
                    flags.add(new RedFlag(
                        "DOMAIN_MISMATCH",
                        WEIGHT_DOMAIN_MISMATCH,
                        "[Technical] Domain mismatch: Company name doesn't match domain",
                        String.format("Company: '%s', Domain: '%s'", company, domain)
                    ));
                    log.debug("   ⚠️  DOMAIN_MISMATCH detected");
                }
            }
        }
    }

    /**
     * C3: Detect AI-Generated Text (Perfection)
     * Overly formal, no personalization, perfect grammar
     */
    private void detectAIPerfection(String textLower, List<RedFlag> flags) {
        int aiMarkerCount = 0;
        Matcher matcher = AI_PERFECTION_PATTERN.matcher(textLower);
        while (matcher.find()) {
            aiMarkerCount++;
        }
        
        // If multiple formal markers found + no personalization
        if (aiMarkerCount >= 3 && !textLower.contains("your name") && !textLower.contains("dear ")) {
            flags.add(new RedFlag(
                "AI_PERFECTION",
                WEIGHT_AI_PERFECTION,
                "[Technical] Possible AI-generated text: Overly formal language with no personalization",
                "Found " + aiMarkerCount + " formal markers without personalization"
            ));
            log.debug("   ℹ️  AI_PERFECTION detected");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CATEGORY D: BEHAVIORAL & CONTEXTUAL RED FLAG DETECTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * D1: Detect Extreme Urgency
     * "Immediate joining", "Last few seats", "Urgent"
     */
    private void detectExtremeUrgency(String textLower, List<RedFlag> flags) {
        if (EXTREME_URGENCY_PATTERN.matcher(textLower).find()) {
            flags.add(new RedFlag(
                "EXTREME_URGENCY",
                WEIGHT_EXTREME_URGENCY,
                "[Behavioral] Extreme urgency detected: Pressures quick response (immediate joining, limited seats)",
                extractEvidence(textLower, EXTREME_URGENCY_PATTERN)
            ));
            log.debug("   ⚠️  EXTREME_URGENCY detected");
        }
    }

    /**
     * D2: Detect Process Inconsistency (with Behavioral Weighting Adjustment)
     * Technical role without interview mention
     * 
     * BEHAVIORAL WEIGHTING ADJUSTMENT:
     * - Base weight: 0.35 (MEDIUM risk)
     * - IF company is verified/strong: weight reduced to 0.08 (5-10% weight)
     * - Rationale: Real high-level companies often skip interview details to keep posts concise
     * - Example: "Stripe" verified company → 0.08 weight (trust the lack of detail)
     * - Example: Unknown company → 0.35 weight (suspicious omission)
     */
    private void detectProcessInconsistency(String textLower, List<RedFlag> flags) {
        detectProcessInconsistency(textLower, flags, false);  // Default to unverified
    }

    /**
     * D2: Detect Process Inconsistency (with Behavioral Weighting Adjustment)
     * Overloaded version with company verification context
     */
    private void detectProcessInconsistency(String textLower, List<RedFlag> flags, boolean isCompanyVerified) {
        boolean isTechRole = textLower.contains("software") || textLower.contains("developer") || 
                            textLower.contains("engineer") || textLower.contains("technical");
        boolean hasInterview = INTERVIEW_MARKER_PATTERN.matcher(textLower).find();
        
        if (isTechRole && !hasInterview && textLower.length() < 500) {
            // BEHAVIORAL WEIGHTING ADJUSTMENT
            double weight = isCompanyVerified ? 0.08 : WEIGHT_PROCESS_INCONSISTENCY;  // 0.08 vs 0.35
            String description = isCompanyVerified ? 
                "[Behavioral] Process omitted (LOW risk - trusted company): Technical role without interview details (normal for high-level companies)" :
                "[Behavioral] Process inconsistency: Technical role without mention of interview/assessment";
            
            flags.add(new RedFlag(
                "PROCESS_INCONSISTENCY",
                weight,
                description,
                "Tech role detected but no interview process mentioned" + 
                (isCompanyVerified ? " (Company verified - accepting omission)" : "")
            ));
            log.debug("   {} PROCESS_INCONSISTENCY detected (weight: {}, company verified: {})", 
                isCompanyVerified ? "ℹ️ " : "⚠️ ", weight, isCompanyVerified);
        }
    }

    /**
     * D3: Detect Global/Local Mismatch
     * Location mismatch (e.g., "London job" + "401k" or "W2")
     */
    private void detectGlobalLocalMismatch(String textLower, List<RedFlag> flags) {
        boolean hasGlobalLocation = GLOBAL_LOCATION_PATTERN.matcher(textLower).find();
        boolean hasLocalBenefit = LOCAL_BENEFIT_PATTERN.matcher(textLower).find();
        
        if (hasGlobalLocation && hasLocalBenefit) {
            flags.add(new RedFlag(
                "GLOBAL_LOCAL_MISMATCH",
                WEIGHT_GLOBAL_LOCAL_MISMATCH,
                "[Behavioral] Geographic mismatch: Global location with US-specific benefits (W2, 401k, H1B)",
                "Location: " + extractEvidence(textLower, GLOBAL_LOCATION_PATTERN) +
                " | Benefit: " + extractEvidence(textLower, LOCAL_BENEFIT_PATTERN)
            ));
            log.debug("   ⚠️  GLOBAL_LOCAL_MISMATCH detected");
        }
    }

    /**
     * D4: Detect Social Proof Gap
     * Unknown company + suspicious domain = low credibility
     */
    private void detectSocialProofGap(String textLower, List<RedFlag> flags) {
        // Heuristic: No mention of prestigious companies, no portfolio links, no team info
        boolean hasCompanyCredibility = textLower.contains("founded") || textLower.contains("years in business") ||
                                       textLower.contains("clients") || textLower.contains("team");
        boolean hasSuspiciousDomain = textLower.contains(".xyz") || textLower.contains(".tk") || 
                                     textLower.contains(".ml") || textLower.contains(".icu");
        
        if (!hasCompanyCredibility && hasSuspiciousDomain) {
            flags.add(new RedFlag(
                "SOCIAL_PROOF_GAP",
                WEIGHT_SOCIAL_PROOF_GAP,
                "[Behavioral] Social proof gap: Unknown company + suspicious domain",
                "No company credibility indicators + suspicious domain extension"
            ));
            log.debug("   ⚠️  SOCIAL_PROOF_GAP detected");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // LEGACY METHODS (backward compatibility)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Legacy: Detect high salary red flag
     */
    private void detectHighSalary(String jobText, List<RedFlag> flags) {
        // Look for salary amounts in the job text
        Pattern salaryPattern = Pattern.compile(
            "\\$([0-9]+[.,][0-9]{3}|[0-9]{5,})", Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = salaryPattern.matcher(jobText);
        while (matcher.find()) {
            String salaryStr = matcher.group(1).replaceAll("[,.]", "");
            try {
                double salary = Double.parseDouble(salaryStr);
                if (salary > SALARY_THRESHOLD) {
                    flags.add(new RedFlag(
                        "HIGH_SALARY",
                        WEIGHT_HIGH_SALARY,
                        "Unusually high salary offer (potential bait-and-switch)",
                        String.format("$%.0f/year", salary)
                    ));
                    return; // Only add one high salary flag
                }
            } catch (NumberFormatException e) {
                // Skip invalid salary format
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SCORING METHODS (ENHANCED)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Calculate enhanced red flag score
     * Formula: sum of all weights / maximum possible (capped at 1.0)
     * 
     * @param flags List of detected red flags
     * @return Red flag score (0-1)
     */
    public double calculateRedFlagScore(List<RedFlag> flags) {
        if (flags == null || flags.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        for (RedFlag flag : flags) {
            score += flag.getWeight();
        }

        // Cap at 1.0
        double finalScore = Math.min(score, 1.0);
        log.info("📊 Red Flag Score Calculation: sum({}) = {} → capped: {}", 
            flags.size(), score, finalScore);
        
        return finalScore;
    }

    /**
     * Apply red flag score to model score using enhanced scaling
     * 
     * Formula:
     *   finalScore = baseModelScore + (redFlagScore * 0.5 * (1 - baseModelScore))
     * 
     * This ensures:
     * - Red flags only INCREASE the fake probability
     * - Effect is proportional to current uncertainty
     * - Already high scores get smaller boost (diminishing returns)
     * - Maximum influence is limited (multiply by 0.5)
     * 
     * @param modelScore Original model score (0-1)
     * @param redFlagScore Red flag score (0-1)
     * @return Adjusted score (0-1)
     */
    public double applyRedFlagScaling(double modelScore, double redFlagScore) {
        if (redFlagScore <= 0) {
            return modelScore;
        }

        // Enhanced scaling: redFlagScore * 0.5 * (1 - modelScore)
        double adjustment = redFlagScore * 0.5 * (1 - modelScore);
        double adjustedScore = modelScore + adjustment;
        
        log.info("⚙️  Red Flag Scaling Applied:");
        log.info("   Model Score: {}", modelScore);
        log.info("   Red Flag Score: {}", redFlagScore);
        log.info("   Adjustment: {} * 0.5 * ({}) = {}", redFlagScore, (1 - modelScore), adjustment);
        log.info("   Final Score: {} → {}", modelScore, Math.min(adjustedScore, 1.0));
        
        return Math.min(adjustedScore, 1.0);
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Extract domain from email address
     * E.g., "user@gmail.com" → "gmail.com"
     */
    private String extractDomainFromEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(atIndex + 1) : "";
    }

    /**
     * Check if domain is a generic/free email service
     */
    private boolean isGenericEmailDomain(String domain) {
        domain = domain.toLowerCase();
        return domain.contains("gmail") || domain.contains("yahoo") || 
               domain.contains("outlook") || domain.contains("hotmail") ||
               domain.contains("aol") || domain.contains("mail.com") ||
               domain.contains("protonmail") || domain.contains("yandex");
    }

    /**
     * Concatenate two evidence strings
     */
    private String concatEvidence(String evidence1, String evidence2) {
        if (evidence1 == null || evidence1.isEmpty()) return evidence2;
        if (evidence2 == null || evidence2.isEmpty()) return evidence1;
        return evidence1 + " | " + evidence2;
    }

    /**
     * NEGATION HANDLER UTILITY
     * Check if there are negation modifiers ("No", "Not", "Never", "Avoid") within N words of a specific position
     * 
     * Used to detect legitimate warnings like "Do NOT contact via Telegram" (GREEN flag)
     * vs fraudulent channels like "Contact on Telegram" (RED flag)
     * 
     * @param text The full text to search
     * @param targetPosition The character position of the flag trigger (e.g., "telegram")
     * @param wordsAround Number of words to check before/after (typically 3)
     * @return true if negation found nearby, false otherwise
     */
    private boolean hasNegationModifierNearby(String text, int targetPosition, int wordsAround) {
        try {
            // Extract a window of text around the target position
            // Estimate: ~5 chars per word, so multiply by 5 for character count
            int windowSize = wordsAround * 5;
            int start = Math.max(0, targetPosition - windowSize);
            int end = Math.min(text.length(), targetPosition + windowSize);
            
            String window = text.substring(start, end);
            
            // Check if negation modifier exists in this window
            Matcher negationMatcher = NEGATION_MODIFIER_PATTERN.matcher(window);
            boolean hasNegation = negationMatcher.find();
            
            if (hasNegation) {
                log.debug("   → Negation detected in window: '{}'", window);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking negation modifiers: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract a snippet of evidence from text that matches the pattern
     * Returns context around the match (20 chars before and after)
     */
    private String extractEvidence(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int start = Math.max(0, matcher.start() - 15);
            int end = Math.min(text.length(), matcher.end() + 15);
            String snippet = text.substring(start, end);
            return "..." + snippet + "...";
        }
        return "Matched in posting";
    }

    /**
     * Format red flags for logging and notes
     */
    public String formatRedFlagsForNote(List<RedFlag> flags, double redFlagScore) {
        if (flags == null || flags.isEmpty()) {
            return "✅ No red flags detected";
        }

        StringBuilder note = new StringBuilder();
        note.append("🚩 RED FLAGS DETECTED (Score: ").append(String.format("%.1f%%", redFlagScore * 100)).append(")\n");
        note.append("════════════════════════════════════════════════════════\n");

        // Group by category
        double financialTotal = 0, communicationTotal = 0, technicalTotal = 0, behavioralTotal = 0;
        
        for (RedFlag flag : flags) {
            note.append("• [").append(flag.getType()).append("] (weight: ");
            note.append(String.format("%.1f%%", flag.getWeight() * 100));
            note.append(")\n  ").append(flag.getDescription()).append("\n");
            
            // Track category totals
            String type = flag.getType();
            if (type.contains("PAY") || type.contains("PAYMENT") || type.contains("EQUIPMENT") || 
                type.contains("BENEFIT") || type.contains("SALARY")) {
                financialTotal += flag.getWeight();
            } else if (type.contains("CHANNEL") || type.contains("EMAIL") || type.contains("DOMAIN") || 
                       type.contains("PHISHING") || type.contains("SPOOFING")) {
                communicationTotal += flag.getWeight();
            } else if (type.contains("HYPHENATED") || type.contains("MISMATCH") || type.contains("AI")) {
                technicalTotal += flag.getWeight();
            } else if (type.contains("URGENCY") || type.contains("PROCESS") || type.contains("GLOBAL") || 
                       type.contains("SOCIAL")) {
                behavioralTotal += flag.getWeight();
            }
        }

        note.append("\n════════════════════════════════════════════════════════\n");
        note.append("📊 CATEGORY BREAKDOWN:\n");
        if (financialTotal > 0) note.append("  💰 Financial: ").append(String.format("%.1f%%", financialTotal * 100)).append("\n");
        if (communicationTotal > 0) note.append("  💬 Communication: ").append(String.format("%.1f%%", communicationTotal * 100)).append("\n");
        if (technicalTotal > 0) note.append("  🔧 Technical: ").append(String.format("%.1f%%", technicalTotal * 100)).append("\n");
        if (behavioralTotal > 0) note.append("  👤 Behavioral: ").append(String.format("%.1f%%", behavioralTotal * 100)).append("\n");

        return note.toString();
    }
}
