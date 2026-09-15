package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.key.DefaultKeyConfigurationValidator;
import com.sinchana.reconciliation.key.ReconciliationConfiguration;
import com.sinchana.reconciliation.model.DataSet;
import com.sinchana.reconciliation.reconcile.ReconciliationEngine;
import com.sinchana.reconciliation.reconcile.ReconciliationMetadata;
import com.sinchana.reconciliation.reconcile.ReconciliationResult;
import com.sinchana.reconciliation.exception.ReportNotFoundException;
import com.sinchana.reconciliation.reporting.ExcelReportExporter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class ReconciliationService {
    private final UploadedDataSetService dataSetService;
    private final DefaultKeyConfigurationValidator configurationValidator;
    private final ReconciliationEngine engine;
    private final ExcelReportExporter reportExporter;
    private final Map<String, ReconciliationResult> completedRuns = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<String> completedRunOrder = new ConcurrentLinkedDeque<>();
    private static final int MAX_COMPLETED_RUNS = 20;

    public ReconciliationService(UploadedDataSetService dataSetService,
                                 DefaultKeyConfigurationValidator configurationValidator,
                                 ReconciliationEngine engine,
                                 ExcelReportExporter reportExporter) {
        this.dataSetService = dataSetService;
        this.configurationValidator = configurationValidator;
        this.engine = engine;
        this.reportExporter = reportExporter;
    }

    public ReconciliationResult reconcile(MultipartFile source, MultipartFile target,
                                          ReconciliationConfiguration configuration) {
        DataSet sourceDataSet = dataSetService.read(source);
        DataSet targetDataSet = dataSetService.read(target);
        configurationValidator.validate(configuration, sourceDataSet, targetDataSet);
        ReconciliationResult reconciled = engine.reconcile(sourceDataSet, targetDataSet, configuration);
        String runId = UUID.randomUUID().toString();
        ReconciliationMetadata metadata = new ReconciliationMetadata(
                runId,
                sourceDataSet.name(),
                targetDataSet.name(),
                configuration.sourceKey(),
                configuration.targetKey(),
                Instant.now());
        ReconciliationResult result = new ReconciliationResult(reconciled.records(), metadata);
        synchronized (completedRunOrder) {
            completedRuns.put(runId, result);
            completedRunOrder.addLast(runId);
            while (completedRunOrder.size() > MAX_COMPLETED_RUNS) {
                String expiredRun = completedRunOrder.pollFirst();
                if (expiredRun != null) completedRuns.remove(expiredRun);
            }
        }
        return result;
    }

    public byte[] report(String runId) {
        ReconciliationResult result = completedRuns.get(runId);
        if (result == null) throw new ReportNotFoundException(runId);
        return reportExporter.export(result);
    }
}
