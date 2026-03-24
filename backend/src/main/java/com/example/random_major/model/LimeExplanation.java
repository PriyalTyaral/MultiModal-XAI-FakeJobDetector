package com.example.random_major.model;

/**
 * Represents a single LIME (Local Interpretable Model-agnostic Explanations) feature.
 * Each instance pairs a word/token with its contribution weight toward the FAKE class.
 *
 * Positive weight  → word pushes prediction toward FAKE (suspicious)
 * Negative weight  → word pushes prediction toward REAL (safe)
 */
public class LimeExplanation {

    private String word;
    private double weight;

    public LimeExplanation() {}

    public LimeExplanation(String word, double weight) {
        this.word = word;
        this.weight = weight;
    }

    // ── Getters ───────────────────────────────────────────
    public String getWord() { return word; }
    public double getWeight() { return weight; }

    // ── Setters ───────────────────────────────────────────
    public void setWord(String word) { this.word = word; }
    public void setWeight(double weight) { this.weight = weight; }

    @Override
    public String toString() {
        return String.format("LimeExplanation{word='%s', weight=%.6f}", word, weight);
    }
}
