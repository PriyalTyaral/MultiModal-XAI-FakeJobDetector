package com.example.random_major.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.random_major.model.RedFlag;

/**
 * RedFlagDetectionService: Detects suspicious patterns in job postings
 * 
 * Identifies indicators of fake/fraudulent jobs:
 * - High salary offers
 * - Payment requests (fees, deposits, checks)
 * - Telegram/WhatsApp contacts
 * - Urgency phrases (act now, limited positions, etc.)
 */
@Service
public class RedFlagDetectionService {

    private static final Logger log = LoggerFactory.getLogger(RedFlagDetectionService.class);

    // ── Weight Constants ────────────────────────────────────────
    private static final double WEIGHT_PAYMENT_REQUEST = 0.4;
    private static final double WEIGHT_TELEGRAM = 0.3;
    private static final double WEIGHT_HIGH_SALARY = 0.2;
    private static final double WEIGHT_URGENCY = 0.1;

    // ── Pattern Constants ────────────────────────────────────────
    private static final Pattern PAYMENT_PATTERN = Pattern.compile(
        "\\b(fee|deposit|check|transfer|wire|payment|crypto|bitcoin|upfront|advance payment)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TELEGRAM_PATTERN = Pattern.compile(
        "\\b(telegram|@|whatsapp|wechat|viber|signal)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern URGENCY_PATTERN = Pattern.compile(
        "\\b(act now|limited|hurry|urgent|asap|immediately|quickly|don't miss|expires|deadline|limited positions)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Salary threshold: 150% of median US salary (~$60k/year)
    private static final double SALARY_THRESHOLD = 90000;

    // ───────────────────────────────────────────────────────
    // MAIN DETECTION METHOD
    // ───────────────────────────────────────────────────────
    /**
     * Detect all red flags in a job posting
     * 
     * @param jobText The job posting text
     * @return List of detected red flags
     */
    public List<RedFlag> detectRedFlags(String jobText) {
        List<RedFlag> flags = new ArrayList<>();

        if (jobText == null || jobText.trim().isEmpty()) {
            return flags;
        }

        String text = jobText.toLowerCase();

        // Detect each type of red flag
        detectPaymentRequest(text, flags);
        detectTelegram(text, flags);
        detectHighSalary(jobText, flags);
        detectUrgency(text, flags);

        log.info("🚩 Detected {} red flags in job posting", flags.size());
        for (RedFlag flag : flags) {
            log.info("  • {}", flag);
        }

        return flags;
    }

    // ───────────────────────────────────────────────────────
    // RED FLAG DETECTION METHODS
    // ───────────────────────────────────────────────────────

    /**
     * Detect payment request red flag
     */
    private void detectPaymentRequest(String text, List<RedFlag> flags) {
        if (PAYMENT_PATTERN.matcher(text).find()) {
            flags.add(new RedFlag(
                "PAYMENT_REQUEST",
                WEIGHT_PAYMENT_REQUEST,
                "Job requires upfront payment or unusual payment request",
                extractEvidence(text, PAYMENT_PATTERN)
            ));
        }
    }

    /**
     * Detect Telegram/WhatsApp contact red flag
     */
    private void detectTelegram(String text, List<RedFlag> flags) {
        if (TELEGRAM_PATTERN.matcher(text).find()) {
            flags.add(new RedFlag(
                "TELEGRAM",
                WEIGHT_TELEGRAM,
                "Suspicious contact method (Telegram, WhatsApp, etc.)",
                extractEvidence(text, TELEGRAM_PATTERN)
            ));
        }
    }

    /**
     * Detect high salary red flag
     */
    private void detectHighSalary(String jobText, List<RedFlag> flags) {
        // Look for salary amounts in the job text
        Pattern salaryPattern = Pattern.compile(
            "\\$([0-9]+[.,][0-9]{3}|[0-9]{5,})", Pattern.CASE_INSENSITIVE
        );

        java.util.regex.Matcher matcher = salaryPattern.matcher(jobText);
        while (matcher.find()) {
            String salaryStr = matcher.group(1).replaceAll("[,.]", "");
            try {
                double salary = Double.parseDouble(salaryStr);
                if (salary > SALARY_THRESHOLD) {
                    flags.add(new RedFlag(
                        "HIGH_SALARY",
                        WEIGHT_HIGH_SALARY,
                        "Unusually high salary offer (potential bait-and-switch)",
                        String.format("${:,.0f}/year", salary)
                    ));
                    return; // Only add one high salary flag
                }
            } catch (NumberFormatException e) {
                // Skip invalid salary format
            }
        }
    }

    /**
     * Detect urgency phrases red flag
     */
    private void detectUrgency(String text, List<RedFlag> flags) {
        if (URGENCY_PATTERN.matcher(text).find()) {
            flags.add(new RedFlag(
                "URGENCY",
                WEIGHT_URGENCY,
                "Suspicious urgency language (pressuring candidates)",
                extractEvidence(text, URGENCY_PATTERN)
            ));
        }
    }

    // ───────────────────────────────────────────────────────
    // SCORING METHODS
    // ───────────────────────────────────────────────────────

    /**
     * Calculate red flag score (weighted sum of detected flags, capped at 1.0)
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

        // Cap at 1.0 (but weights should sum to at most 1.0 anyway)
        return Math.min(score, 1.0);
    }

    /**
     * Apply red flag score to model score using scaling formula:
     * adjustedScore = modelScore + redFlagScore * (1 - modelScore)
     * 
     * This ensures:
     * - redFlagScore only increases the final score
     * - Effect is proportional to current uncertainty
     * - Already high scores get a smaller boost
     * 
     * @param modelScore Original model score (0-1)
     * @param redFlagScore Red flag score (0-1)
     * @return Adjusted score (0-1)
     */
    public double applyRedFlagScaling(double modelScore, double redFlagScore) {
        if (redFlagScore <= 0) {
            return modelScore;
        }

        double adjustedScore = modelScore + (redFlagScore * (1 - modelScore));
        return Math.min(adjustedScore, 1.0);
    }

    // ───────────────────────────────────────────────────────
    // UTILITY METHODS
    // ───────────────────────────────────────────────────────

    /**
     * Extract a snippet of evidence from text that matches the pattern
     */
    private String extractEvidence(String text, Pattern pattern) {
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int start = Math.max(0, matcher.start() - 20);
            int end = Math.min(text.length(), matcher.end() + 20);
            return "..." + text.substring(start, end) + "...";
        }
        return "Found in posting";
    }

    /**
     * Format red flags for logging
     */
    public String formatRedFlagsForNote(List<RedFlag> flags, double redFlagScore) {
        if (flags.isEmpty()) {
            return "✅ No red flags detected";
        }

        StringBuilder note = new StringBuilder();
        note.append("🚩 Red Flags Detected (Score: ").append(String.format("%.1f%%", redFlagScore * 100)).append("):\n");

        for (RedFlag flag : flags) {
            note.append("  • ").append(flag.getDescription()).append("\n");
        }

        return note.toString();
    }
}
