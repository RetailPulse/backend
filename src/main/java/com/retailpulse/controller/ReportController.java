package com.retailpulse.controller;

import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.service.report.InventoryTransactionReportService;
import com.retailpulse.service.report.ProductReportService;
import com.retailpulse.util.DateUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private static final String INVALID_DATE_RANGE = "INVALID_DATE_RANGE";

    private final InventoryTransactionReportService inventoryTransactionReportService;
    private final ProductReportService productReportService;

    public ReportController(InventoryTransactionReportService inventoryTransactionReportService, ProductReportService productReportService) {
        this.inventoryTransactionReportService = inventoryTransactionReportService;
        this.productReportService = productReportService;
    }

    @GetMapping("/inventory-transactions")
    public List<InventoryTransactionDto> getInventoryTransactions(@RequestParam String startDateTime,
                                                                  @RequestParam String endDateTime,
                                                                  @RequestParam String dateTimeFormat) {
        logger.info("Fetching all inventory transactions");

        if (startDateTime == null || startDateTime.isBlank()) {
            throw new ApplicationException(INVALID_DATE_RANGE, "Start date time cannot be after end date time");
        }

        if (endDateTime == null || endDateTime.isBlank()) {
            throw new ApplicationException(INVALID_DATE_RANGE, "Start date time cannot be after end date time");
        }

        Instant startInstant;
        Instant endInstant;
        try {
            startInstant = DateUtil.convertStringToInstant(startDateTime, dateTimeFormat);
            endInstant = DateUtil.convertStringToInstant(endDateTime, dateTimeFormat);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new ApplicationException(INVALID_DATE_RANGE, "Invalid date time format");
        }

        if (startInstant.isAfter(endInstant)) {
            throw new ApplicationException(INVALID_DATE_RANGE, "Start date time cannot be after end date time");
        }

        return inventoryTransactionReportService.getInventoryTransactions(startInstant, endInstant);
    }

    @GetMapping("/inventory-transactions/export")
    public void exportInventoryTransactionReport(HttpServletResponse response,
                                                 @RequestParam String startDateTime,
                                                 @RequestParam String endDateTime,
                                                 @RequestParam String dateTimeFormat,
                                                 @RequestParam String format) throws IOException {
        if (startDateTime == null || startDateTime.isBlank() ||
                endDateTime == null || endDateTime.isBlank()) {
            throw new ApplicationException(INVALID_DATE_RANGE, "Date time parameters cannot be blank");
        }

        Instant start;
        Instant end;
        try {
            start = DateUtil.convertStringToInstant(startDateTime, dateTimeFormat);
            end = DateUtil.convertStringToInstant(endDateTime, dateTimeFormat);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new ApplicationException(INVALID_DATE_RANGE, "Invalid date time format");
        }
        if (start.isAfter(end)) {
            throw new ApplicationException(INVALID_DATE_RANGE, "Start date time cannot be after end date time");
        }
        // Delegate export to the report service using the common template method
        inventoryTransactionReportService.exportReport(response, start, end, format);
    }

    @GetMapping("/products/export")
    public void exportProductReport(HttpServletResponse response,
                                    @RequestParam("format") String format) throws IOException {

        productReportService.exportReport(response, null, null, format);
    }

}
