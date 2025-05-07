package com.retailpulse.service;

import com.retailpulse.service.dataExtractor.TableDataExtractor;
import com.retailpulse.service.exportReportHelper.ExcelReportExportService;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExcelReportExportServiceTest {

    private ExcelReportExportService<String> excelReportExportService;
    private HttpServletResponse mockResponse;
    private TableDataExtractor<String> mockExtractor;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        String title = "Test Report";
        String[] headers = {"Header1", "Header2", "Header3"};
        mockExtractor = mock(TableDataExtractor.class);
        excelReportExportService = new ExcelReportExportService<>(title, headers, mockExtractor);

        mockResponse = mock(HttpServletResponse.class);
        outputStream = new ByteArrayOutputStream();
        try {
            when(mockResponse.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testExportReport() throws Exception {
        // Arrange
        Instant start = Instant.now();
        Instant end = Instant.now();
        List<String> data = Arrays.asList("Row1", "Row2", "Row3");

        when(mockExtractor.getRowData(anyString(), anyInt())).thenAnswer(invocation -> {
            String item = invocation.getArgument(0);
            int serialNumber = invocation.getArgument(1);
            return new Object[]{serialNumber, item, "ExtraData"};
        });

        // Act
        excelReportExportService.exportReport(mockResponse, start, end, data);

        // Assert
        verify(mockResponse).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(mockResponse).setHeader(eq("Content-Disposition"), contains("report_"));

        // Verify the output stream contains the Excel file
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("Report", workbook.getSheetAt(0).getSheetName());
        }

        // Verify extractor was called for each row
        verify(mockExtractor, times(data.size())).getRowData(anyString(), anyInt());
    }

    // Mock implementation of ServletOutputStream for testing
    private static class MockServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final OutputStream outputStream;

        public MockServletOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true; // Always ready for testing purposes
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            // No-op for testing purposes
        }
    }
}
