package com.sinchana.reconciliation.reader;

import com.sinchana.reconciliation.exception.DataReadException;
import com.sinchana.reconciliation.exception.DataValidationException;
import com.sinchana.reconciliation.exception.UnsupportedFileTypeException;
import com.sinchana.reconciliation.model.DataSet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

@Component
public class ExcelDataReader implements DataReader {
    private final DataFormatter formatter = new DataFormatter();

    @Override
    public DataSet read(Path path) throws IOException {
        if (path == null) {
            throw new DataReadException("A file path is required");
        }
        String fileName = path.getFileName() == null ? path.toString() : path.getFileName().toString();
        try (InputStream input = Files.newInputStream(path);
             Workbook workbook = workbookFor(input, fileName)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new DataValidationException("The workbook does not contain a worksheet");
            }
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null || header.getLastCellNum() <= 0) {
                throw new DataValidationException("The workbook must have a header row");
            }
            int columnCount = header.getLastCellNum();
            List<String> columns = new ArrayList<>(columnCount);
            Set<String> seenHeaders = new HashSet<>();
            for (int i = 0; i < columnCount; i++) {
                String value = textValue(header.getCell(i), workbook.getCreationHelper().createFormulaEvaluator());
                if (value == null || value.isBlank()) {
                    throw new DataValidationException("Column headers cannot be blank (column " + (i + 1) + ")");
                }
                String column = value.trim();
                if (!seenHeaders.add(column.toLowerCase(Locale.ROOT))) {
                    throw new DataValidationException("Column headers must be unique: " + column);
                }
                columns.add(column);
            }

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            List<java.util.Map<String, Object>> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlank(row, columnCount, evaluator)) {
                    continue;
                }
                if (row.getLastCellNum() > columnCount) {
                    for (int i = columnCount; i < row.getLastCellNum(); i++) {
                        if (!isBlank(row.getCell(i), evaluator)) {
                            throw new DataValidationException("Row " + (rowIndex + 1)
                                    + " contains a value outside the header columns");
                        }
                    }
                }
                LinkedHashMap<String, Object> values = new LinkedHashMap<>();
                for (int column = 0; column < columnCount; column++) {
                    values.put(columns.get(column), cellValue(row.getCell(column), evaluator));
                }
                rows.add(values);
            }
            return new DataSet(fileName, columns, rows,
                    java.util.Map.of("sheetName", sheet.getSheetName(), "format", extension(fileName)));
        } catch (DataValidationException exception) {
            throw exception;
        } catch (UnsupportedFileTypeException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new DataReadException("The workbook could not be read: " + fileName, exception);
        }
    }

    private Workbook workbookFor(InputStream input, String fileName) throws IOException {
        String ext = extension(fileName);
        if ("xlsx".equals(ext)) return new XSSFWorkbook(input);
        if ("xls".equals(ext)) return new HSSFWorkbook(input);
        throw new UnsupportedFileTypeException("Unsupported file type. Only .xls and .xlsx are supported");
    }

    private String extension(String name) {
        int dot = name == null ? -1 : name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private Object cellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.FORMULA) {
            var evaluated = evaluator.evaluate(cell);
            if (evaluated == null) return null;
            if (evaluated.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return dateValue(cell.getDateCellValue());
            }
            return switch (evaluated.getCellType()) {
                case STRING -> evaluated.getStringValue();
                case BOOLEAN -> evaluated.getBooleanValue();
                case NUMERIC -> evaluated.getNumberValue();
                default -> null;
            };
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return DateUtil.isCellDateFormatted(cell) ? dateValue(cell.getDateCellValue()) : cell.getNumericCellValue();
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case ERROR -> formatter.formatCellValue(cell, evaluator);
            default -> null;
        };
    }

    private String textValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        String value = formatter.formatCellValue(cell, evaluator);
        return value == null ? null : value.trim();
    }

    private Object dateValue(Date value) {
        LocalDateTime dateTime = value.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return dateTime.toLocalTime().equals(LocalTime.MIDNIGHT)
                ? dateTime.toLocalDate() : dateTime;
    }

    private boolean isBlank(Row row, int columns, FormulaEvaluator evaluator) {
        for (int i = 0; i < columns; i++) if (!isBlank(row.getCell(i), evaluator)) return false;
        return true;
    }

    private boolean isBlank(Cell cell, FormulaEvaluator evaluator) {
        Object value = cellValue(cell, evaluator);
        return value == null || (value instanceof String string && string.isBlank());
    }
}
