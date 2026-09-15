package com.sinchana.reconciliation.reporting;

import com.sinchana.reconciliation.reconcile.FieldDifference;
import com.sinchana.reconciliation.reconcile.RecordReconciliation;
import com.sinchana.reconciliation.reconcile.ReconciliationResult;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

@Component
public class ExcelReportExporter {
    private static final String EMPTY_VALUE = "";
    private static final int MAX_CELL_LENGTH = 32_767;

    public byte[] export(ReconciliationResult result) {
        if (result == null) throw new IllegalArgumentException("A reconciliation result is required");
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            writeSummary(workbook, styles, result);
            writeRecords(workbook, styles, result);
            writeDifferences(workbook, styles, result);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("The Excel report could not be generated", exception);
        }
    }

    private void writeSummary(Workbook workbook, Styles styles, ReconciliationResult result) {
        Sheet sheet = workbook.createSheet("Summary");
        sheet.createFreezePane(0, 3);
        sheet.setColumnWidth(0, 28 * 256);
        sheet.setColumnWidth(1, 52 * 256);
        Row title = sheet.createRow(0);
        title.createCell(0).setCellValue("Reconciliation Report");
        title.getCell(0).setCellStyle(styles.title);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

        section(sheet, 2, "Reconciliation Run", styles.section);
        row(sheet, 3, "Property", "Value", styles.header);
        var metadata = result.metadata();
        row(sheet, 4, "Run ID", value(metadata.runId()), styles.label);
        row(sheet, 5, "Source filename", value(metadata.sourceFileName()), styles.label);
        row(sheet, 6, "Target filename", value(metadata.targetFileName()), styles.label);
        row(sheet, 7, "Source reconciliation key", value(metadata.sourceKey()), styles.label);
        row(sheet, 8, "Target reconciliation key", value(metadata.targetKey()), styles.label);
        row(sheet, 9, "Execution timestamp", value(metadata.executedAt()), styles.label);

        section(sheet, 11, "Reconciliation Results", styles.section);
        row(sheet, 12, "Result", "Count", styles.header);
        row(sheet, 13, "Total Logical Results", result.summary().total(), styles.metric);
        row(sheet, 14, "Matched", result.summary().matched(), styles.metric);
        row(sheet, 15, "Changed", result.summary().changed(), styles.metric);
        row(sheet, 16, "Missing", result.summary().missing(), styles.metric);
        row(sheet, 17, "Extra", result.summary().extra(), styles.metric);
        row(sheet, 18, "Duplicate", result.summary().duplicate(), styles.metric);
        row(sheet, 19, "Invalid Key", result.summary().invalidKey(), styles.metric);

        section(sheet, 21, "Result Rates", styles.section);
        row(sheet, 22, "Result", "Rate", styles.header);
        long total = result.summary().total();
        percentageRow(sheet, 23, "Matched %", result.summary().matched(), total, styles);
        percentageRow(sheet, 24, "Changed %", result.summary().changed(), total, styles);
        percentageRow(sheet, 25, "Missing %", result.summary().missing(), total, styles);
        percentageRow(sheet, 26, "Extra %", result.summary().extra(), total, styles);
        percentageRow(sheet, 27, "Duplicate %", result.summary().duplicate(), total, styles);
        percentageRow(sheet, 28, "Invalid Key %", result.summary().invalidKey(), total, styles);
        Row note = sheet.createRow(30);
        note.createCell(0).setCellValue(
                "Rates are calculated as a percentage of total logical reconciliation results.");
        note.getCell(0).setCellStyle(styles.note);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(30, 30, 0, 1));
    }

    private void writeRecords(Workbook workbook, Styles styles, ReconciliationResult result) {
        Sheet sheet = workbook.createSheet("Record Results");
        String[] headers = {"Status", "Reconciliation Key", "Source Row", "Target Row",
                "Field Difference Count", "Source Rows", "Target Rows", "Source Record Count",
                "Target Record Count"};
        writeHeader(sheet, headers, styles);
        int rowIndex = 1;
        for (RecordReconciliation record : result.records()) {
            Row row = sheet.createRow(rowIndex++);
            text(row, 0, record.status(), styles.body);
            value(row, 1, record.key(), styles.body);
            value(row, 2, record.sourceRowIndex(), styles.body);
            value(row, 3, record.targetRowIndex(), styles.body);
            value(row, 4, record.differences().size(), styles.body);
            text(row, 5, join(record.sourceRowIndices()), styles.body);
            text(row, 6, join(record.targetRowIndices()), styles.body);
            value(row, 7, record.sourceRecords().size(), styles.body);
            value(row, 8, record.targetRecords().size(), styles.body);
        }
        finishTable(sheet, headers.length, rowIndex);
    }

    private void writeDifferences(Workbook workbook, Styles styles, ReconciliationResult result) {
        Sheet sheet = workbook.createSheet("Field Differences");
        String[] headers = {"Reconciliation Key", "Status", "Source Row Indexes",
                "Target Row Indexes", "Field / Column", "Source Value", "Target Value"};
        writeHeader(sheet, headers, styles);
        int rowIndex = 1;
        for (RecordReconciliation record : result.records()) {
            for (FieldDifference difference : record.differences()) {
                Row row = sheet.createRow(rowIndex++);
                value(row, 0, record.key(), styles.body);
                text(row, 1, record.status(), styles.body);
                text(row, 2, join(record.sourceRowIndices()), styles.body);
                text(row, 3, join(record.targetRowIndices()), styles.body);
                text(row, 4, difference.column(), styles.body);
                value(row, 5, difference.sourceValue(), styles.body);
                value(row, 6, difference.targetValue(), styles.body);
            }
        }
        finishTable(sheet, headers.length, rowIndex);
    }

    private void writeHeader(Sheet sheet, String[] headers, Styles styles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
            row.getCell(i).setCellStyle(styles.header);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));
        int[] widths = headers.length == 9
                ? new int[] {14, 24, 12, 12, 18, 18, 18, 18, 18}
                : new int[] {24, 14, 18, 18, 24, 34, 34};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);
    }

    private void finishTable(Sheet sheet, int columnCount, int rowCount) {
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowCount - 1), 0, columnCount - 1));
    }

    private void section(Sheet sheet, int index, String title, CellStyle style) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(title);
        row.getCell(0).setCellStyle(style);
        row.createCell(1).setCellStyle(style);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(index, index, 0, 1));
    }

    private void percentageRow(Sheet sheet, int index, String label, long count, long total,
                               Styles styles) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(label);
        row.getCell(0).setCellStyle(styles.metric);
        row.createCell(1).setCellValue(total == 0 ? 0 : (double) count / total);
        row.getCell(1).setCellStyle(styles.percentage);
    }

    private void row(Sheet sheet, int index, String first, Object second, CellStyle style) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(first);
        row.createCell(1);
        row.getCell(0).setCellStyle(style);
        row.getCell(1).setCellStyle(style);
        setValue(row.getCell(1), second);
    }

    private void value(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(style);
        setValue(cell, value);
    }

    private void text(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(style);
        cell.setCellValue(truncate(value == null ? EMPTY_VALUE : String.valueOf(value)));
    }

    private void setValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else {
            cell.setCellValue(truncate(String.valueOf(value)));
        }
    }

    private String value(Object value) {
        return value == null ? EMPTY_VALUE : String.valueOf(value);
    }

    private String join(List<?> values) {
        StringJoiner joiner = new StringJoiner(", ");
        values.forEach(value -> joiner.add(String.valueOf(value)));
        return joiner.toString();
    }

    private String truncate(String value) {
        return value.length() <= MAX_CELL_LENGTH ? value : value.substring(0, MAX_CELL_LENGTH);
    }

    private static final class Styles {
        private final CellStyle title;
        private final CellStyle header;
        private final CellStyle label;
        private final CellStyle metric;
        private final CellStyle body;
        private final CellStyle section;
        private final CellStyle percentage;
        private final CellStyle note;

        private Styles(Workbook workbook) {
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            title = workbook.createCellStyle();
            title.setFont(titleFont);

            header = workbook.createCellStyle();
            header.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_TEAL.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            header.setFont(headerFont);
            header.setAlignment(HorizontalAlignment.CENTER);

            label = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            label.setFont(labelFont);
            metric = workbook.createCellStyle();
            metric.setBorderBottom(BorderStyle.THIN);
            body = workbook.createCellStyle();
            body.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
            body.setWrapText(true);

            section = workbook.createCellStyle();
            section.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            section.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            section.setFont(sectionFont);

            percentage = workbook.createCellStyle();
            percentage.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
            percentage.setBorderBottom(BorderStyle.THIN);

            note = workbook.createCellStyle();
            note.setWrapText(true);
            Font noteFont = workbook.createFont();
            noteFont.setItalic(true);
            note.setFont(noteFont);
        }
    }
}
