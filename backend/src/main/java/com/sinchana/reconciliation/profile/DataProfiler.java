package com.sinchana.reconciliation.profile;

import com.sinchana.reconciliation.model.DataSet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class DataProfiler {
    public DataProfile profile(DataSet dataSet) {
        if (dataSet == null) throw new IllegalArgumentException("Dataset is required");
        List<ColumnProfile> profiles = new ArrayList<>();
        for (String column : dataSet.columns()) {
            List<Object> values = dataSet.rows().stream().map(row -> row.get(column)).toList();
            profiles.add(profileColumn(column, values));
        }
        return new DataProfile(dataSet.rowCount(), dataSet.columnCount(), profiles, dataSet.metadata());
    }

    private ColumnProfile profileColumn(String name, List<Object> values) {
        int nullCount = 0, blankCount = 0, nonNullCount = 0;
        List<Object> populated = new ArrayList<>();
        for (Object value : values) {
            if (value == null) { nullCount++; continue; }
            nonNullCount++;
            if (value instanceof String string && string.isBlank()) { blankCount++; continue; }
            populated.add(value);
        }
        DataType type = detectType(populated);
        List<Double> numeric = populated.stream().filter(Number.class::isInstance)
                .map(value -> ((Number) value).doubleValue()).sorted().toList();
        Double mean = numeric.isEmpty() ? null : numeric.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        Double median = numeric.isEmpty() ? null : median(numeric);
        Double deviation = numeric.isEmpty() ? null : Math.sqrt(
                numeric.stream().mapToDouble(n -> Math.pow(n - mean, 2)).average().orElse(0));
        Object min = comparableMin(populated);
        Object max = comparableMax(populated);
        List<Integer> lengths = populated.stream().filter(String.class::isInstance)
                .map(value -> ((String) value).length()).sorted().toList();
        return new ColumnProfile(name, type, values.size(), nonNullCount, nullCount, blankCount,
                new LinkedHashSet<>(populated).size(), min, max, mean, median, deviation,
                lengths.isEmpty() ? null : lengths.get(0),
                lengths.isEmpty() ? null : lengths.get(lengths.size() - 1),
                populated.stream().limit(5).toList());
    }

    private DataType detectType(List<Object> values) {
        if (values.isEmpty()) return DataType.UNKNOWN;
        if (values.stream().allMatch(String.class::isInstance)) {
            List<String> strings = values.stream().map(String.class::cast).map(String::trim).toList();
            if (strings.stream().allMatch(value -> value.equalsIgnoreCase("true")
                    || value.equalsIgnoreCase("false"))) return DataType.BOOLEAN;
            try {
                List<BigDecimal> numbers = strings.stream().map(BigDecimal::new).toList();
                return numbers.stream().allMatch(value -> value.stripTrailingZeros().scale() <= 0)
                        ? DataType.INTEGER : DataType.DECIMAL;
            } catch (NumberFormatException ignored) {
                return DataType.STRING;
            }
        }
        if (values.stream().allMatch(Boolean.class::isInstance)) return DataType.BOOLEAN;
        if (values.stream().allMatch(LocalDate.class::isInstance)) return DataType.DATE;
        if (values.stream().allMatch(LocalDateTime.class::isInstance)) return DataType.DATETIME;
        if (values.stream().allMatch(Number.class::isInstance)) {
            return values.stream().map(Number.class::cast)
                    .allMatch(value -> value.doubleValue() % 1 == 0) ? DataType.INTEGER : DataType.DECIMAL;
        }
        return DataType.MIXED;
    }

    private Double median(List<Double> values) {
        int middle = values.size() / 2;
        return values.size() % 2 == 0 ? (values.get(middle - 1) + values.get(middle)) / 2 : values.get(middle);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object comparableMin(List<Object> values) {
        return values.stream().filter(value -> value instanceof Comparable)
                .min((a, b) -> compareValues(a, b)).orElse(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object comparableMax(List<Object> values) {
        return values.stream().filter(value -> value instanceof Comparable)
                .max((a, b) -> compareValues(a, b)).orElse(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareValues(Object left, Object right) {
        if (left.getClass().equals(right.getClass())) return ((Comparable) left).compareTo(right);
        return String.valueOf(left).compareTo(String.valueOf(right));
    }
}
