package com.retailpulse.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.retailpulse.DTO.InventoryTransactionDto;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportExportService extends AbstractReportExportService {
    private Document document;

    // Define headers for your report table
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
        response.setContentType("application/pdf");

        // Generate a filename with current datetime (e.g., inventory_report_20250423_153012.pdf)
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedDate = now.format(formatter);
        String fileName = "inventory_report_" + formattedDate + ".pdf";

        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    @Override
    protected void writeReportHeader(HttpServletResponse response, Instant start, Instant end) throws IOException {
        document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
        } catch (DocumentException e) {
            throw new IOException(e);
        }
        document.open();
        
        // Write report title
        Paragraph title = new Paragraph("Inventory Transaction Report", getTitleFont());
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        
        // Use a DateTimeFormatter that matches your DTO's transaction date format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        // Display "Report Generated On" using the current time
        String generatedOn = dtf.format(LocalDateTime.now());
        Paragraph generatedOnParagraph = new Paragraph("Report Generated On: " + generatedOn, getSubtitleFont());
        document.add(generatedOnParagraph);
        
        // Format start and end date from your parameters
        String startFormatted = dtf.format(LocalDateTime.ofInstant(start, ZoneId.systemDefault()));
        String endFormatted = dtf.format(LocalDateTime.ofInstant(end, ZoneId.systemDefault()));
        
        // Write subtitle with "Start Date:" and "End Date:" labels
        Paragraph subtitle = new Paragraph("Start Date: " + startFormatted + "    End Date: " + endFormatted, getSubtitleFont());
        document.add(subtitle);
        
        document.add(Chunk.NEWLINE);
    }

    @Override
    protected void writeTableHeader(HttpServletResponse response) throws IOException {
        // Build the table header using the ReportAbstract’s template method logic
        PdfPTable headerTable = new PdfPTable(headers.length);
        headerTable.setWidthPercentage(100);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, getFontContent()));
            headerTable.addCell(cell);
        }
        document.add(headerTable);
    }

    @Override
    protected void writeTableData(HttpServletResponse response, List<InventoryTransactionDto> data) throws IOException {
        // Build the table data directly within this service
        PdfPTable dataTable = new PdfPTable(headers.length);
        dataTable.setWidthPercentage(100);

        int number = 0;
        for (InventoryTransactionDto item : data) {
            number++;

            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(number), getFontContent()));
            dataTable.addCell(cell);

            cell = new PdfPCell(new Phrase(item.transactionId(), getFontContent()));
            dataTable.addCell(cell);

            String product = item.product() != null ? item.product().sku() : "";
            cell = new PdfPCell(new Phrase(product, getFontContent()));
            dataTable.addCell(cell);

            String description = item.product() != null ? item.product().description() : "";
            cell = new PdfPCell(new Phrase(description, getFontContent()));
            dataTable.addCell(cell);

            String quantity = item.productPricing() != null ? String.valueOf(item.productPricing().quantity()) : "";
            cell = new PdfPCell(new Phrase(quantity, getFontContent()));
            dataTable.addCell(cell);

            String source = item.source() != null ? item.source().name() : "";
            cell = new PdfPCell(new Phrase(source, getFontContent()));
            dataTable.addCell(cell);

            String destination = item.destination() != null ? item.destination().name() : "";
            cell = new PdfPCell(new Phrase(destination, getFontContent()));
            dataTable.addCell(cell);

            String transactionDate = item.transactionDateTime();
            cell = new PdfPCell(new Phrase(transactionDate, getFontContent()));
            dataTable.addCell(cell);
        }
        document.add(dataTable);
    }

    @Override
    protected void finalizeReport(HttpServletResponse response) throws IOException {
        document.close();
    }

    // Helper methods for fonts
    private Font getTitleFont() {
        return new Font(Font.HELVETICA, 16, Font.BOLD);
    }

    private Font getSubtitleFont() {
        return new Font(Font.HELVETICA, 12, Font.NORMAL);
    }

    private Font getFontContent() {
        return new Font(Font.HELVETICA, 10, Font.NORMAL);
    }
}
