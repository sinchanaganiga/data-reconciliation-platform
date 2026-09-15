package com.sinchana.reconciliation.reporting;

import com.sinchana.reconciliation.reconcile.FieldDifference;
import com.sinchana.reconciliation.reconcile.RecordReconciliation;
import com.sinchana.reconciliation.reconcile.ReconciliationMetadata;
import com.sinchana.reconciliation.reconcile.ReconciliationResult;
import com.sinchana.reconciliation.reconcile.ReconciliationStatus;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExcelReportExporterTest {
    private final ExcelReportExporter exporter = new ExcelReportExporter();

    @Test
    void exportsSummaryRecordsAndFieldDifferencesFromTheResult() throws Exception {
        var source = new LinkedHashMap<String, Object>();
        source.put("source_key", "A");
        source.put("source_value", "old");
        var target = new LinkedHashMap<String, Object>();
        target.put("target_key", "A");
        target.put("target_value", "new");
        var changed = new RecordReconciliation(
                ReconciliationStatus.CHANGED, "A", 1, 1, source, target,
                List.of(1), List.of(1), List.of(source), List.of(target),
                List.of(new FieldDifference("value", "old", "new")));
        var duplicate = new RecordReconciliation(
                ReconciliationStatus.DUPLICATE, "D", 2, 2, source, target,
                List.of(2, 3), List.of(2), List.of(source, source), List.of(target), List.of());
        var result = new ReconciliationResult(
                List.of(changed, duplicate),
                new ReconciliationMetadata("run", "source.xlsx", "target.xlsx",
                        "source_key", "target_key", Instant.parse("2026-01-01T00:00:00Z")));

        byte[] bytes = exporter.export(result);

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertEquals(List.of("Summary", "Record Results", "Field Differences"),
                    List.of(workbook.getSheetName(0), workbook.getSheetName(1), workbook.getSheetName(2)));
            var summary = workbook.getSheet("Summary");
            assertEquals("run", summary.getRow(4).getCell(1).getStringCellValue());
            assertEquals(2, summary.getRow(13).getCell(1).getNumericCellValue());
            assertEquals(1, summary.getRow(15).getCell(1).getNumericCellValue());
            assertEquals(0.5, summary.getRow(24).getCell(1).getNumericCellValue());
            assertEquals("CHANGED", workbook.getSheet("Record Results").getRow(1).getCell(0).getStringCellValue());
            assertEquals("DUPLICATE", workbook.getSheet("Record Results").getRow(2).getCell(0).getStringCellValue());
            var recordHeader = workbook.getSheet("Record Results").getRow(0);
            assertEquals(9, recordHeader.getLastCellNum());
            assertFalse(recordHeader.getCell(9) != null);
            assertFalse(recordHeader.getCell(10) != null);
            assertEquals("2, 3", workbook.getSheet("Record Results").getRow(2).getCell(5).getStringCellValue());
            var difference = workbook.getSheet("Field Differences");
            assertEquals("Status", difference.getRow(0).getCell(1).getStringCellValue());
            assertEquals("1", difference.getRow(1).getCell(2).getStringCellValue());
            assertEquals("1", difference.getRow(1).getCell(3).getStringCellValue());
            assertEquals("value", difference.getRow(1).getCell(4).getStringCellValue());
            assertEquals("old", difference.getRow(1).getCell(5).getStringCellValue());
            assertEquals("new", difference.getRow(1).getCell(6).getStringCellValue());
        }
    }

    @Test
    void exportsAnEmptyResultWithAllExpectedSheets() throws Exception {
        byte[] bytes = exporter.export(new ReconciliationResult(List.of()));

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertNotNull(workbook.getSheet("Summary"));
            assertEquals(1, workbook.getSheet("Record Results").getPhysicalNumberOfRows());
            assertEquals(1, workbook.getSheet("Field Differences").getPhysicalNumberOfRows());
        }
    }

    @Test
    void exportsZeroRatesForAnEmptyResult() throws Exception {
        byte[] bytes = exporter.export(new ReconciliationResult(List.of()));

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            var summary = workbook.getSheet("Summary");
            assertEquals(0, summary.getRow(13).getCell(1).getNumericCellValue());
            assertEquals(0, summary.getRow(23).getCell(1).getNumericCellValue());
            assertEquals(0, summary.getRow(28).getCell(1).getNumericCellValue());
        }
    }

    @Test
    void exportsInvalidKeySummaryAndRowMetadata() throws Exception {
        var source = new LinkedHashMap<String, Object>();
        source.put("key", " ");
        var invalid = new RecordReconciliation(
                ReconciliationStatus.INVALID_KEY, null, 4, null, source, null,
                List.of(4), List.of(), List.of(source), List.of(), List.of());

        byte[] bytes = exporter.export(new ReconciliationResult(List.of(invalid)));

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertEquals(1, workbook.getSheet("Summary").getRow(19).getCell(1).getNumericCellValue());
            var row = workbook.getSheet("Record Results").getRow(1);
            assertEquals("INVALID_KEY", row.getCell(0).getStringCellValue());
            assertEquals(4, row.getCell(2).getNumericCellValue());
            assertEquals(0, row.getCell(8).getNumericCellValue());
        }
    }
}
