package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.key.ReconciliationConfiguration;
import com.sinchana.reconciliation.reconcile.ReconciliationResult;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {
    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping(value = "/execute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReconciliationResult execute(@RequestPart("source") MultipartFile source,
                                        @RequestPart("target") MultipartFile target,
                                        @RequestPart("configuration") ReconciliationConfiguration configuration) {
        return reconciliationService.reconcile(source, target, configuration);
    }

    @GetMapping("/{runId}/report")
    public ResponseEntity<byte[]> report(@org.springframework.web.bind.annotation.PathVariable String runId) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header("Content-Disposition",
                        ContentDisposition.attachment().filename("reconciliation-report.xlsx").build().toString())
                .body(reconciliationService.report(runId));
    }
}
