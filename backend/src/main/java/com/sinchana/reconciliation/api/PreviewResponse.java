package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.profile.ColumnProfile;
import com.sinchana.reconciliation.profile.DataProfile;
import com.sinchana.reconciliation.profile.DataType;

import java.util.List;
import java.util.Map;

public record PreviewResponse(List<DatasetPreview> datasets) {
    public record DatasetPreview(String fileName, int rowCount, int columnCount,
                                 List<String> columns, List<Map<String, Object>> rows,
                                 ProfileDto profile) {}

    public record ProfileDto(int rowCount, int columnCount, List<ColumnProfileDto> columns,
                             Map<String, Object> metadata) {
        static ProfileDto from(DataProfile profile) {
            return new ProfileDto(profile.rowCount(), profile.columnCount(),
                    profile.columns().stream().map(ColumnProfileDto::from).toList(), profile.metadata());
        }
    }

    public record ColumnProfileDto(String name, DataType dataType, int totalCount, int nonNullCount,
                                   int nullCount, int blankCount, int populatedCount, int distinctCount,
                                   int duplicateCount, double nullPercentage, double blankPercentage,
                                   double uniquenessPercentage,
                                   Object min, Object max, Double mean, Double median,
                                   Double standardDeviation, Integer minLength, Integer maxLength,
                                   List<Object> sampleValues) {
        static ColumnProfileDto from(ColumnProfile profile) {
            return new ColumnProfileDto(profile.name(), profile.dataType(), profile.totalCount(),
                    profile.nonNullCount(), profile.nullCount(), profile.blankCount(),
                    profile.populatedCount(), profile.distinctCount(), profile.duplicateCount(),
                    profile.nullPercentage(), profile.blankPercentage(), profile.uniquenessPercentage(),
                    profile.min(), profile.max(),
                    profile.mean(), profile.median(), profile.standardDeviation(),
                    profile.minLength(), profile.maxLength(), profile.sampleValues());
        }
    }
}
