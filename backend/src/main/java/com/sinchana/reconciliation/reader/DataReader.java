package com.sinchana.reconciliation.reader;

import com.sinchana.reconciliation.model.DataSet;
import java.io.IOException;
import java.nio.file.Path;

/** Contract for file-format readers. */
public interface DataReader {
    DataSet read(Path path) throws IOException;
}
