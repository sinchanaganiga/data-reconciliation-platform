package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.exception.DataReadException;
import com.sinchana.reconciliation.exception.DataValidationException;
import com.sinchana.reconciliation.model.DataSet;
import com.sinchana.reconciliation.reader.DataReaderFactory;
import com.sinchana.reconciliation.validation.DataSetValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

@Service
public class UploadedDataSetService {
    private final DataReaderFactory readerFactory;
    private final DataSetValidator validator;

    public UploadedDataSetService(DataReaderFactory readerFactory, DataSetValidator validator) {
        this.readerFactory = readerFactory;
        this.validator = validator;
    }

    public DataSet read(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new DataValidationException("Uploaded files must not be empty");
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new DataValidationException("Uploaded files must have a filename");
        }
        String name = Path.of(originalName).getFileName().toString();
        if (name.isBlank()) throw new DataValidationException("Uploaded files must have a filename");
        Path temporary = null;
        try {
            temporary = Files.createTempFile("reconciliation-", "-" + name);
            file.transferTo(temporary);
            if (!Files.isRegularFile(temporary) || !Files.isReadable(temporary)) {
                throw new DataReadException("Uploaded file is not readable: " + name);
            }
            DataSet readDataSet = readerFactory.getReader(temporary).read(temporary);
            var metadata = new LinkedHashMap<String, Object>(readDataSet.metadata());
            metadata.put("fileName", name);
            DataSet dataSet = new DataSet(name, readDataSet.columns(), readDataSet.rows(), metadata);
            validator.validate(dataSet);
            return dataSet;
        } catch (com.sinchana.reconciliation.exception.ReconciliationException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new DataReadException("Unable to read " + name, exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) { }
            }
        }
    }
}
