package com.sinchana.reconciliation.validation;

import com.sinchana.reconciliation.exception.DataValidationException;
import com.sinchana.reconciliation.model.DataSet;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class DataSetValidator {
    public void validate(DataSet dataSet) {
        if (dataSet == null) throw new DataValidationException("Dataset is required");
        if (dataSet.columns().isEmpty()) throw new DataValidationException("The dataset must have a header row");
        Set<String> seen = new HashSet<>();
        for (String column : dataSet.columns()) {
            if (column == null || column.isBlank()) throw new DataValidationException("Column names cannot be blank");
            if (!seen.add(column.trim().toLowerCase(Locale.ROOT))) {
                throw new DataValidationException("Column names must be unique: " + column);
            }
        }
        int count = dataSet.columnCount();
        for (var row : dataSet.rows()) {
            if (row == null || row.size() != count) {
                throw new DataValidationException("Every data row must contain " + count + " values");
            }
            for (String column : dataSet.columns()) {
                if (!row.containsKey(column)) {
                    throw new DataValidationException("Every data row must contain all header columns");
                }
            }
        }
    }
}
