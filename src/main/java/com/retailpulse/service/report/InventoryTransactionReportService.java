package com.retailpulse.service.report;

import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.DTO.mapper.InventoryTransactionReportMapper;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.repository.InventoryTransactionRepository;
import com.retailpulse.service.dataExtractor.InventoryTransactionDataExtractor;
import com.retailpulse.service.dataExtractor.TableDataExtractor;
import com.retailpulse.service.exportReportHelper.ExcelReportExportService;
import com.retailpulse.service.exportReportHelper.PdfReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public void exportReport(HttpServletResponse response, Instant start, Instant end, String format) throws IOException {
        List<InventoryTransactionDto> data = getInventoryTransactions(start, end);
        String[] headers = new String[]{
                "S/No.", "Transaction ID", "SKU", "Description", "Quantity", "Source", "Destination", "Transaction Date Time"
        };
        String title = "Inventory Transaction Report";

        TableDataExtractor<InventoryTransactionDto> extractor = new InventoryTransactionDataExtractor();
        if ("pdf".equalsIgnoreCase(format)) {
            PdfReportExportService<InventoryTransactionDto> exportService = new PdfReportExportService<>(title, headers, extractor);
            exportService.exportReport(response, start, end, data);
        } else if ("excel".equalsIgnoreCase(format)) {
            ExcelReportExportService<InventoryTransactionDto> exportService = new ExcelReportExportService<>(title, headers, extractor);
            exportService.exportReport(response, start, end, data);
        } else {
            throw new ApplicationException("INVALID_FORMAT", "Unsupported export format: " + format);
        }
    }
}
