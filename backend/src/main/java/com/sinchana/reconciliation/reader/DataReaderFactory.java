package com.sinchana.reconciliation.reader;

import com.sinchana.reconciliation.exception.UnsupportedFileTypeException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Locale;

@Component
public class DataReaderFactory {
    private final ExcelDataReader excelDataReader;

    public DataReaderFactory(ExcelDataReader excelDataReader) {
        this.excelDataReader = excelDataReader;
    }

    public DataReader getReader(Path path) {
        String name = path == null || path.getFileName() == null ? "" : path.getFileName().toString();
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return excelDataReader;
        throw new UnsupportedFileTypeException("Unsupported file type. Only .xls and .xlsx are supported");
    }

    public DataReader getReader(String extension) {
        String normalized = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (normalized.startsWith(".")) normalized = normalized.substring(1);
        if ("xls".equals(normalized) || "xlsx".equals(normalized)) return excelDataReader;
        throw new UnsupportedFileTypeException("Unsupported file type. Only .xls and .xlsx are supported");
    }

    public DataReader readerFor(Path path) {
        return getReader(path);
    }

    public DataReader createReader(Path path) {
        return getReader(path);
    }
}
