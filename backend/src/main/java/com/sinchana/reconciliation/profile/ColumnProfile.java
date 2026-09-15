package com.sinchana.reconciliation.profile;

import java.util.List;

public final class ColumnProfile {
    private final String name;
    private final DataType dataType;
    private final int totalCount;
    private final int nonNullCount;
    private final int nullCount;
    private final int blankCount;
    private final int distinctCount;
    private final int duplicateCount;
    private final Object min;
    private final Object max;
    private final Double mean;
    private final Double median;
    private final Double standardDeviation;
    private final Integer minLength;
    private final Integer maxLength;
    private final List<Object> sampleValues;

    public ColumnProfile(String name, DataType dataType, int totalCount, int nonNullCount,
                         int nullCount, int blankCount, int distinctCount, Object min, Object max,
                         Double mean, Double median, Double standardDeviation,
                         Integer minLength, Integer maxLength, List<Object> sampleValues) {
        this.name = name; this.dataType = dataType; this.totalCount = totalCount;
        this.nonNullCount = nonNullCount; this.nullCount = nullCount; this.blankCount = blankCount;
        this.distinctCount = distinctCount;
        this.duplicateCount = Math.max(0, totalCount - nullCount - blankCount - distinctCount);
        this.min = min; this.max = max; this.mean = mean;
        this.median = median; this.standardDeviation = standardDeviation; this.minLength = minLength;
        this.maxLength = maxLength; this.sampleValues = List.copyOf(sampleValues);
    }

    public ColumnProfile(String name, int blankCount, int populatedCount) {
        this(name, DataType.UNKNOWN, blankCount + populatedCount, populatedCount, 0,
                blankCount, 0, null, null, null, null, null, null, null, List.of());
    }

    public String name() { return name; }
    public String columnName() { return name; }
    public DataType dataType() { return dataType; }
    public DataType type() { return dataType; }
    public DataType detectedType() { return dataType; }
    public int totalCount() { return totalCount; }
    public int nonNullCount() { return nonNullCount; }
    public int count() { return totalCount; }
    public int nonBlankCount() { return populatedCount(); }
    public int nullCount() { return nullCount; }
    public int blankCount() { return blankCount; }
    public int populatedCount() { return nonNullCount - blankCount; }
    public int distinctCount() { return distinctCount; }
    public int uniqueCount() { return distinctCount; }
    public int duplicateCount() { return duplicateCount; }
    public int missingCount() { return blankCount; }
    public double nullPercentage() { return totalCount == 0 ? 0 : (double) nullCount / totalCount; }
    public double blankPercentage() { return totalCount == 0 ? 0 : (double) blankCount / totalCount; }
    public double uniquenessPercentage() { return totalCount == 0 ? 0 : (double) distinctCount / totalCount; }
    public Object min() { return min; }
    public Object max() { return max; }
    public Object minimum() { return min; }
    public Object maximum() { return max; }
    public Double mean() { return mean; }
    public Double average() { return mean; }
    public Double median() { return median; }
    public Double standardDeviation() { return standardDeviation; }
    public Double stdDev() { return standardDeviation; }
    public Integer minLength() { return minLength; }
    public Integer maxLength() { return maxLength; }
    public List<Object> sampleValues() { return sampleValues; }
    public String getName() { return name; }
    public String getColumnName() { return name; }
    public DataType getDataType() { return dataType; }
    public int getTotalCount() { return totalCount; }
    public int getNonNullCount() { return nonNullCount; }
    public int getNullCount() { return nullCount; }
    public int getBlankCount() { return blankCount; }
    public int getPopulatedCount() { return populatedCount(); }
    public int getDistinctCount() { return distinctCount; }
    public int getUniqueCount() { return distinctCount; }
    public int getDuplicateCount() { return duplicateCount; }
    public int getMissingCount() { return blankCount; }
    public double getNullPercentage() { return nullPercentage(); }
    public double getBlankPercentage() { return blankPercentage(); }
    public double getUniquenessPercentage() { return uniquenessPercentage(); }
    public Object getMin() { return min; }
    public Object getMax() { return max; }
    public Double getMean() { return mean; }
    public Double getAverage() { return mean; }
    public Double getMedian() { return median; }
    public Double getStandardDeviation() { return standardDeviation; }
    public Double getStdDev() { return standardDeviation; }
    public Integer getMinLength() { return minLength; }
    public Integer getMaxLength() { return maxLength; }
    public List<Object> getSampleValues() { return sampleValues; }
}
