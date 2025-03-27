package com.retailpulse.controller;

import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.DTO.InventoryTransactionReportDto;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.service.InventoryTransactionReportService;
import com.retailpulse.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/reports/inventory-transactions")
public class InventoryTransactionReportController {
    private static final  Logger logger = LoggerFactory.getLogger(InventoryTransactionReportController.class);
    private static final String INVALID_DATE_RANGE = "INVALID_DATE_RANGE";

    private final InventoryTransactionReportService inventoryTransactionReportService;

    public InventoryTransactionReportController(InventoryTransactionReportService inventoryTransactionReportService) {
        this.inventoryTransactionReportService = inventoryTransactionReportService;
    }

    @GetMapping
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

        List<InventoryTransactionDto> inventoryTransactions = inventoryTransactionReportService.getInventoryTransactions(startInstant, endInstant);

        return inventoryTransactions;
    }

}
