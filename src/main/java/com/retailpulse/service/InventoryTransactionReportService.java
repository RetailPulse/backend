package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.DTO.mapper.InventoryTransactionReportMapper;
import com.retailpulse.repository.InventoryTransactionRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryTransactionReportService {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryTransactionReportService.class);
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryTransactionReportService(InventoryTransactionRepository inventoryTransactionRepository) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    public List<InventoryTransactionDto> getInventoryTransactions(Instant startDateTime, Instant endDateTime) {
        LOG.info("Fetching all inventory transactions");
        return inventoryTransactionRepository.findAllWithProductAndBusinessEntity(startDateTime, endDateTime)
                .stream()
                .map(InventoryTransactionReportMapper::toInventoryTransactionDto)
                .collect(Collectors.toList());
    }

    // New method that uses the Template Method to export report
    public void exportReport(HttpServletResponse response, Instant start, Instant end, String format) throws IOException {
        List<InventoryTransactionDto> data = getInventoryTransactions(start, end);
        if ("pdf".equalsIgnoreCase(format)) {
            PdfReportExportService exportService = new PdfReportExportService();
            exportService.exportReport(response, start, end, data);
        } else if ("excel".equalsIgnoreCase(format)) {
            ExcelReportExportService exportService = new ExcelReportExportService();
            exportService.exportReport(response, start, end, data);
        } else {
            throw new ApplicationException("INVALID_FORMAT", "Unsupported export format: " + format);
        }
    }
}
