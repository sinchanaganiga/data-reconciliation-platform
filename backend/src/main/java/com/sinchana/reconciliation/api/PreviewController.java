package com.sinchana.reconciliation.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class PreviewController {
    private final PreviewService previewService;

    public PreviewController(PreviewService previewService) {
        this.previewService = previewService;
    }

    @PostMapping(value = "/reconciliation/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PreviewResponse preview(@RequestPart("source") MultipartFile source,
                                   @RequestPart("target") MultipartFile target) {
        return previewService.preview(source, target);
    }

    /** Kept as a short-lived compatibility alias for existing clients. */
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PreviewResponse legacyPreview(@RequestPart("files") MultipartFile[] files) {
        if (files == null || files.length != 2) {
            throw new com.sinchana.reconciliation.exception.DataValidationException("Exactly source and target files are required");
        }
        return previewService.preview(files[0], files[1]);
    }
}
