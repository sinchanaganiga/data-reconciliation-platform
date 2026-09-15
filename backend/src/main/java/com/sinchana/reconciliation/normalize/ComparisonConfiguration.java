package com.sinchana.reconciliation.normalize;

/**
 * Centralized rules used by the future normalization and reconciliation stages.
 */
public record ComparisonConfiguration(
        boolean trimWhitespace,
        boolean normalizeInternalWhitespace,
        boolean caseSensitive,
        boolean normalizeNumbers,
        boolean normalizeDates,
        boolean nullEqualsBlank) {

    public static ComparisonConfiguration defaults() {
        return new ComparisonConfiguration(true, false, true, true, true, false);
    }
}
