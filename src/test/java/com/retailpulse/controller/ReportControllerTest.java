package com.retailpulse.controller;

import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.service.report.InventoryTransactionReportService;
import com.retailpulse.service.report.ProductReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
/*
 * Unit tests for ReportController covering the following scenarios:
 * <p>
 * Inventory Transaction Reports:
 * - Happy path: valid start/end date with expected service call
 * - Blank start or end date input: should throw ApplicationException
 * - Invalid date format: should throw ApplicationException
 * - Start date after end date: should throw ApplicationException
 * - Start date equal to end date: service call should still succeed
 * <p>
 * Export Inventory Transaction Report:
 * - Happy path: verifies correct arguments passed to service
 * - Blank start or end date: should throw ApplicationException
 * - Invalid date format: should throw ApplicationException
 * - Start date after end date: should throw ApplicationException
 * <p>
 * Export Product Report:
 * - Happy path: verifies service call with expected parameters
 * - IOException during export: should propagate exception correctly
 */
public class ReportControllerTest {
    private final String validStart = "2023-10-10T12:00:00Z";
    private final String validEnd = "2023-10-11T12:00:00Z";
    private final String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    @Mock
    private InventoryTransactionReportService inventoryTransactionReportService;
    @Mock
    private ProductReportService productReportService;
    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    public void setUp() {
        // reportController is initialized with @InjectMocks.
    }

    @Test
    public void testGetInventoryTransactions_HappyCase() {
        List<InventoryTransactionDto> dummyList = new ArrayList<>();
        when(inventoryTransactionReportService.getInventoryTransactions(any(), any()))
                .thenReturn(dummyList);

        List<InventoryTransactionDto> result = reportController.getInventoryTransactions(validStart, validEnd, dateTimeFormat);
        assertThat(result).isEqualTo(dummyList);
        verify(inventoryTransactionReportService, times(1)).getInventoryTransactions(any(), any());
    }

    @Test
    public void testGetInventoryTransactions_BlankStartDate() {
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reportController.getInventoryTransactions(" ", validEnd, dateTimeFormat);
        });
        assertThat(exception.getMessage()).contains("Start date time parameter cannot be blank");
    }

    @Test
    public void testGetInventoryTransactions_BlankEndDate() {
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reportController.getInventoryTransactions(validStart, " ", dateTimeFormat);
        });
        assertThat(exception.getMessage()).contains("End date time parameter cannot be blank");
    }

    @Test
    public void testGetInventoryTransactions_InvalidDateFormat() {
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reportController.getInventoryTransactions("invalid", validEnd, dateTimeFormat);
        });
        assertThat(exception.getMessage()).contains("Invalid date time format");
    }

    @Test
    public void testGetInventoryTransactions_StartAfterEnd() {
        String laterStart = "2023-10-12T12:00:00Z";
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reportController.getInventoryTransactions(laterStart, validEnd, dateTimeFormat);
        });
        assertThat(exception.getMessage()).contains("Start date time cannot be after end date time");
    }

    @Test
    public void testGetInventoryTransactions_StartEqualsEnd() {
        // When start equals end, the service should be called successfully.
        List<InventoryTransactionDto> dummyList = new ArrayList<>();
        when(inventoryTransactionReportService.getInventoryTransactions(any(), any()))
                .thenReturn(dummyList);

        List<InventoryTransactionDto> result = reportController.getInventoryTransactions(validStart, validStart, dateTimeFormat);
        assertThat(result).isEqualTo(dummyList);
        verify(inventoryTransactionReportService, times(1)).getInventoryTransactions(any(), any());
    }

    @Test
    public void testExportInventoryTransactionReport_HappyCase() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);

        reportController.exportInventoryTransactionReport(response, validStart, validEnd, dateTimeFormat, "PDF");

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(inventoryTransactionReportService, times(1))
                .exportReport(eq(response), startCaptor.capture(), endCaptor.capture(), eq("PDF"));
    }

    @Test
    public void testExportInventoryTransactionReport_BlankDate() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        ApplicationException ex1 = assertThrows(ApplicationException.class, () -> {
            reportController.exportInventoryTransactionReport(response, "", validEnd, dateTimeFormat, "PDF");
        });
        assertThat(ex1.getMessage()).contains("Date time parameters cannot be blank");

        ApplicationException ex2 = assertThrows(ApplicationException.class, () -> {
            reportController.exportInventoryTransactionReport(response, validStart, "", dateTimeFormat, "PDF");
        });
        assertThat(ex2.getMessage()).contains("Date time parameters cannot be blank");
    }

    @Test
    public void testExportInventoryTransactionReport_InvalidDateFormat() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reportController.exportInventoryTransactionReport(response, "invalid", validEnd, dateTimeFormat, "PDF");
        });
        assertThat(exception.getMessage()).contains("Invalid date time format");
    }

    @Test
    public void testExportInventoryTransactionReport_StartAfterEnd() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        String laterStart = "2023-10-12T12:00:00Z";
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            reportController.exportInventoryTransactionReport(response, laterStart, validEnd, dateTimeFormat, "PDF");
        });
        assertThat(exception.getMessage()).contains("Start date time cannot be after end date time");
    }

    @Test
    public void testExportProductReport_HappyCase() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);

        reportController.exportProductReport(response, "PDF");

        verify(productReportService, times(1)).exportReport(eq(response), isNull(), isNull(), eq("PDF"));
    }

    @Test
    public void testExportProductReport_IOException() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(new IOException("Export failed")).when(productReportService).exportReport(eq(response), isNull(), isNull(), eq("PDF"));

        IOException thrown = assertThrows(IOException.class, () -> {
            reportController.exportProductReport(response, "PDF");
        });
        assertThat(thrown.getMessage()).contains("Export failed");
    }
}
