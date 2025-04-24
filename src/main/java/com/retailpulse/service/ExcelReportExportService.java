package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionDto;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelReportExportService extends AbstractReportExportService{
    private XSSFWorkbook workbook;
    private Sheet sheet;
    // Define headers for your report table (same as in the PDF service)
    private final String[] headers = new String[]{
            "S/No.",
            "Transaction ID",
            "SKU",
            "Description",
            "Quantity",
            "Source",
            "Destination",
            "Transaction Date Time"
    };

    @Override
    protected void initResponse(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Generate filename with current datetime (e.g., inventory_report_20250423_153012.xlsx)
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String fileName = "inventory_report_" + now.format(formatter) + ".xlsx";

        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    @Override
    protected void writeReportHeader(HttpServletResponse response, Instant start, Instant end) throws IOException {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Inventory Report");

        // Create title row at index 0
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Inventory Transaction Report");

        // Use a DateTimeFormatter matching your DTO's transaction date format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Row 1: Report Generated On
        Row generatedOnRow = sheet.createRow(1);
        Cell generatedOnCell = generatedOnRow.createCell(0);
        String generatedOn = dtf.format(LocalDateTime.now());
        generatedOnCell.setCellValue("Report Generated On: " + generatedOn);

        // Row 2: Start and End Dates (formatted)
        Row dateRow = sheet.createRow(2);
        Cell dateCell = dateRow.createCell(0);
        String startFormatted = dtf.format(LocalDateTime.ofInstant(start, ZoneId.systemDefault()));
        String endFormatted = dtf.format(LocalDateTime.ofInstant(end, ZoneId.systemDefault()));
        dateCell.setCellValue("Start Date: " + startFormatted + "    End Date: " + endFormatted);

        // Optionally, create a blank row (row 3) for spacing before table header
        sheet.createRow(3);
    }

    @Override
    protected void writeTableHeader(HttpServletResponse response) throws IOException {
        // Write header starting at row index 3
        Row headerRow = sheet.createRow(3);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    @Override
    protected void writeTableData(HttpServletResponse response, List<InventoryTransactionDto> data) throws IOException {
        int rowNum = 4;  // Data rows start after the header row
        int number = 0;
        for (InventoryTransactionDto item : data) {
            number++;
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            row.createCell(cellNum++).setCellValue(number);
            row.createCell(cellNum++).setCellValue(item.transactionId());
            String product = item.product() != null ? item.product().sku() : "";
            row.createCell(cellNum++).setCellValue(product);
            String description = item.product() != null ? item.product().description() : "";
            row.createCell(cellNum++).setCellValue(description);
            String quantity = item.productPricing() != null ?
                    String.valueOf(item.productPricing().quantity()) : "";
            row.createCell(cellNum++).setCellValue(quantity);
            String source = item.source() != null ? item.source().name() : "";
            row.createCell(cellNum++).setCellValue(source);
            String destination = item.destination() != null ? item.destination().name() : "";
            row.createCell(cellNum++).setCellValue(destination);
            row.createCell(cellNum++).setCellValue(item.transactionDateTime());
        }
    }

    @Override
    protected void finalizeReport(HttpServletResponse response) throws IOException {
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
