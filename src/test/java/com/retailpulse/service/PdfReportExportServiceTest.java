package com.retailpulse.service;

import com.retailpulse.service.dataExtractor.TableDataExtractor;
import com.retailpulse.service.exportReportHelper.PdfReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class PdfReportExportServiceTest {

    private PdfReportExportService<String> pdfReportExportService;
    private HttpServletResponse mockResponse;
    private TableDataExtractor<String> mockExtractor;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {
        String title = "Test PDF";
        String[] headers = {"Header1", "Header2", "Header3"};
        mockExtractor = mock(TableDataExtractor.class);
        pdfReportExportService = new PdfReportExportService<>(title, headers, mockExtractor);

        mockResponse = mock(HttpServletResponse.class);
        outputStream = new ByteArrayOutputStream();
        when(mockResponse.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
    }

    @Test
    void testExportPdfReport() throws Exception {
        Instant start = Instant.now();
        Instant end = Instant.now();
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");

        when(mockExtractor.getRowData(anyString(), anyInt())).thenAnswer(invocation -> {
            String item = invocation.getArgument(0);
            int index = invocation.getArgument(1);
            return new Object[]{index, item, "MockData"};
        });

        pdfReportExportService.exportReport(mockResponse, start, end, data);

        verify(mockResponse).setContentType("application/pdf");
        verify(mockResponse).setHeader(eq("Content-Disposition"), contains("report_"));
        verify(mockExtractor, times(3)).getRowData(anyString(), anyInt());
    }

    // Inner class to mock ServletOutputStream
    private static class MockServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final OutputStream outputStream;

        public MockServletOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) {
            try {
                outputStream.write(b);
            } catch (Exception ignored) {}
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            // No-op for test
        }
    }
}