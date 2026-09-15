package com.sinchana.reconciliation.normalize;

import com.sinchana.reconciliation.model.DataSet;

import java.util.List;

public record NormalizedDataSet(
        DataSet original,
        List<NormalizedRow> rows) {

    public NormalizedDataSet {
        rows = List.copyOf(rows);
    }
}
