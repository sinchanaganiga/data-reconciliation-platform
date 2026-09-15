package com.sinchana.reconciliation.api;

import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PreviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void requiresTwoFiles() throws Exception {
        var file = new MockMultipartFile("files", "one.txt", "text/plain",
                "not an excel file".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/preview").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void previewsSourceAndTargetParts() throws Exception {
        byte[] workbook = workbookBytes();
        var source = new MockMultipartFile("source", "source.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbook);
        var target = new MockMultipartFile("target", "target.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbook);
        mockMvc.perform(multipart("/api/reconciliation/preview").file(source).file(target))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.datasets.length()").value(2))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.datasets[0].rows[0].Id").value(7.0));
    }

    private byte[] workbookBytes() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Data");
            sheet.createRow(0).createCell(0).setCellValue("Id");
            sheet.getRow(0).createCell(1).setCellValue("Name");
            sheet.createRow(1).createCell(0).setCellValue(7);
            sheet.getRow(1).createCell(1).setCellValue("Ada");
            var output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
