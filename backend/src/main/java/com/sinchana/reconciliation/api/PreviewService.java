package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.exception.DataValidationException;
import com.sinchana.reconciliation.model.DataSet;
import com.sinchana.reconciliation.profile.DataProfiler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PreviewService {
    private static final int PREVIEW_LIMIT = 20;
    private final UploadedDataSetService dataSetService;
    private final DataProfiler profiler;

    public PreviewService(UploadedDataSetService dataSetService, DataProfiler profiler) {
        this.dataSetService = dataSetService;
        this.profiler = profiler;
    }

    public PreviewResponse preview(MultipartFile source, MultipartFile target) {
        if (source == null || target == null) throw new DataValidationException("Source and target files are required");
        return new PreviewResponse(List.of(read(source), read(target)));
    }

    private PreviewResponse.DatasetPreview read(MultipartFile file) {
        DataSet dataSet = dataSetService.read(file);
        var profile = profiler.profile(dataSet);
        return new PreviewResponse.DatasetPreview(dataSet.name(), dataSet.rowCount(), dataSet.columnCount(),
                dataSet.columns(), dataSet.rows().stream().limit(PREVIEW_LIMIT).toList(),
                PreviewResponse.ProfileDto.from(profile));
    }
}
