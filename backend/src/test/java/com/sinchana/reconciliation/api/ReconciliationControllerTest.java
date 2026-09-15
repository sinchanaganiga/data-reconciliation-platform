package com.sinchana.reconciliation.api;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class ReconciliationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void executesReconciliationForUploadedDatasets() throws Exception {
        byte[] workbook = workbookBytes();
        var source = new MockMultipartFile("source", "source.xlsx", "application/octet-stream", workbook);
        var target = new MockMultipartFile("target", "target.xlsx", "application/octet-stream", workbook);
        var configuration = new MockMultipartFile("configuration", "configuration.json", "application/json",
                """
                {"sourceKey":"Id","targetKey":"Id","comparison":{"trimWhitespace":true,"normalizeInternalWhitespace":false,"caseSensitive":true,"normalizeNumbers":true,"normalizeDates":true,"nullEqualsBlank":false}}
                """.getBytes());

        mockMvc.perform(multipart("/api/reconciliation/execute")
                        .file(source).file(target).file(configuration))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].status").value("MATCHED"))
                .andExpect(jsonPath("$.summary.total").value(1))
                .andExpect(jsonPath("$.summary.matched").value(1))
                .andExpect(jsonPath("$.summary.changed").value(0));
    }

    @Test
    void downloadsTheCompletedReconciliationAsAnExcelWorkbook() throws Exception {
        byte[] workbook = workbookBytes();
        var source = new MockMultipartFile("source", "source.xlsx", "application/octet-stream", workbook);
        var target = new MockMultipartFile("target", "target.xlsx", "application/octet-stream", workbook);
        var configuration = new MockMultipartFile("configuration", "configuration.json", "application/json",
                """
                {"sourceKey":"Id","targetKey":"Id","comparison":{"trimWhitespace":true,"normalizeInternalWhitespace":false,"caseSensitive":true,"normalizeNumbers":true,"normalizeDates":true,"nullEqualsBlank":false}}
                """.getBytes());

        var execution = mockMvc.perform(multipart("/api/reconciliation/execute")
                        .file(source).file(target).file(configuration))
                .andExpect(status().isOk())
                .andReturn();
        String runId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(execution.getResponse().getContentAsString())
                .get("metadata").get("runId").asText();

        var report = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/reconciliation/" + runId + "/report")
                .header("Origin", "http://localhost:5173"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
        .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("reconciliation-report.xlsx")));
        try (var generated = WorkbookFactory.create(new ByteArrayInputStream(
                report.andReturn().getResponse().getContentAsByteArray()))) {
            org.junit.jupiter.api.Assertions.assertEquals(3, generated.getNumberOfSheets());
        }
    }

    private byte[] workbookBytes() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Data");
            sheet.createRow(0).createCell(0).setCellValue("Id");
            sheet.getRow(0).createCell(1).setCellValue("Value");
            sheet.createRow(1).createCell(0).setCellValue("A");
            sheet.getRow(1).createCell(1).setCellValue("same");
            var output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
