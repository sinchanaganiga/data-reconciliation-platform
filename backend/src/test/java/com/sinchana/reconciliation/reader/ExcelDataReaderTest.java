package com.sinchana.reconciliation.reader;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExcelDataReaderTest {
    private final ExcelDataReader reader = new ExcelDataReader();

    @Test
    void readsFirstSheetHeadersAndRows() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Data");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Id");
            header.createCell(1).setCellValue("Name");
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue(7);
            row.createCell(1).setCellValue("Ada");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);

            var path = Files.createTempFile("reader-test-", ".xlsx");
            Files.write(path, output.toByteArray());
            var dataSet = reader.read(path);
            assertEquals(path.getFileName().toString(), dataSet.name());
            assertEquals(2, dataSet.columnCount());
            assertEquals(1, dataSet.rowCount());
            assertEquals(7.0, dataSet.rows().get(0).get("Id"));
            Files.deleteIfExists(path);
        }
    }

    @Test
    void rejectsUnsupportedExtension() throws Exception {
        var path = Files.createTempFile("reader-test-", ".csv");
        assertThrows(Exception.class, () -> reader.read(path));
        Files.deleteIfExists(path);
    }

    @Test
    void rejectsBlankAndDuplicateHeaders() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet();
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Id");
            header.createCell(1).setCellValue(" id ");
            var output = new ByteArrayOutputStream();
            workbook.write(output);
            var path = Files.createTempFile("reader-test-", ".xlsx");
            Files.write(path, output.toByteArray());
            assertThrows(com.sinchana.reconciliation.exception.DataValidationException.class,
                    () -> reader.read(path));
            Files.deleteIfExists(path);
        }
    }
}
