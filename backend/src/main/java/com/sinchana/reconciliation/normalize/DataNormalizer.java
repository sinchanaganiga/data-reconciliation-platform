package com.sinchana.reconciliation.normalize;

import com.sinchana.reconciliation.model.DataSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.regex.Pattern;

@Component
public class DataNormalizer {
    private static final Pattern INTERNAL_WHITESPACE = Pattern.compile("\\s+");
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    public NormalizedDataSet normalize(DataSet dataSet, ComparisonConfiguration configuration) {
        ComparisonConfiguration rules = configuration == null
                ? ComparisonConfiguration.defaults() : configuration;
        List<NormalizedRow> rows = IntStream.range(0, dataSet.rows().size())
                .mapToObj(index -> normalizeRow(dataSet.rows().get(index), dataSet.columns(), index, rules))
                .toList();
        return new NormalizedDataSet(dataSet, rows);
    }

    private NormalizedRow normalizeRow(Map<String, Object> row, List<String> columns, int index,
                                       ComparisonConfiguration configuration) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (String column : columns) {
            normalized.put(column, normalizeValue(row.get(column), configuration));
        }
        return new NormalizedRow(index, row, normalized);
    }

    private Object normalizeValue(Object value, ComparisonConfiguration configuration) {
        if (value == null) return null;

        if (value instanceof Number number && configuration.normalizeNumbers()) {
            return canonicalNumber(number.toString());
        }
        if (value instanceof java.util.Date date && configuration.normalizeDates()) {
            return Instant.ofEpochMilli(date.getTime()).toString();
        }
        if (value instanceof LocalDate date && configuration.normalizeDates()) {
            return date.atStartOfDay(ZoneOffset.UTC).toInstant().toString();
        }
        if (value instanceof LocalDateTime dateTime && configuration.normalizeDates()) {
            return dateTime.toInstant(ZoneOffset.UTC).toString();
        }
        if (value instanceof OffsetDateTime dateTime && configuration.normalizeDates()) {
            return dateTime.toInstant().toString();
        }

        if (!(value instanceof String text)) return value;
        String normalized = text;
        if (configuration.trimWhitespace()) normalized = normalized.trim();
        if (configuration.normalizeInternalWhitespace()) {
            normalized = INTERNAL_WHITESPACE.matcher(normalized).replaceAll(" ");
        }
        if (configuration.nullEqualsBlank() && normalized.isEmpty()) return null;
        if (configuration.normalizeNumbers()) {
            String number = tryNormalizeNumber(normalized);
            if (number != null) return number;
        }
        if (configuration.normalizeDates()) {
            String date = tryNormalizeDate(normalized);
            if (date != null) return date;
        }
        return configuration.caseSensitive() ? normalized : normalized.toLowerCase(Locale.ROOT);
    }

    private String tryNormalizeNumber(String value) {
        try {
            return canonicalNumber(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String canonicalNumber(String value) {
        return new BigDecimal(value).stripTrailingZeros().toPlainString();
    }

    private String tryNormalizeDate(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                if (formatter == DateTimeFormatter.ISO_LOCAL_DATE) {
                    return LocalDate.parse(value, formatter)
                            .atStartOfDay(ZoneOffset.UTC).toInstant().toString();
                }
                if (formatter == DateTimeFormatter.ISO_LOCAL_DATE_TIME) {
                    return LocalDateTime.parse(value, formatter).toInstant(ZoneOffset.UTC).toString();
                }
                return OffsetDateTime.parse(value, formatter).toInstant().toString();
            } catch (DateTimeParseException ignored) {
                // A non-date string remains a string value.
            }
        }
        return null;
    }
}
