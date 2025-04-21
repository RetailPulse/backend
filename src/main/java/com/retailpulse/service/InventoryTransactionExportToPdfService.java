package com.retailpulse.service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.retailpulse.DTO.InventoryTransactionDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class InventoryTransactionExportToPdfService extends ReportAbstract {
    public void writeTableData(PdfPTable table, Object data) {
        // Cast data safely to List<InventoryTransactionDto>
        List<InventoryTransactionDto> list = (List<InventoryTransactionDto>) data;

        // Set table width percentage to auto adjust within paper size
        table.setWidthPercentage(100);
        
        PdfPCell cell = new PdfPCell();
        int number = 0;
        for (InventoryTransactionDto item : list) {
            number++;
            // Column: S/No.
            cell.setPhrase(new Phrase(String.valueOf(number), getFontContent()));
            table.addCell(cell);
            
            // Column: Transaction ID
            cell.setPhrase(new Phrase(item.transactionId(), getFontContent()));
            table.addCell(cell);
            
            // Column: SKU
            String product = item.product() != null ? item.product().sku() : "";
            cell.setPhrase(new Phrase(product, getFontContent()));
            table.addCell(cell);

            // Column: Description
            String description = item.product() != null ? String.valueOf(item.product().description()) : "";
            cell.setPhrase(new Phrase(description, getFontContent()));
            table.addCell(cell);
            
            // Column: Quantity
            String pricing = item.productPricing() != null ? String.valueOf(item.productPricing().quantity()) : "";
            cell.setPhrase(new Phrase(pricing, getFontContent()));
            table.addCell(cell);
            
            // Column: Source
            String source = item.source() != null ? item.source().name() : "";
            cell.setPhrase(new Phrase(source, getFontContent()));
            table.addCell(cell);
            
            // Column: Destination
            String destination = item.destination() != null ? item.destination().name() : "";
            cell.setPhrase(new Phrase(destination, getFontContent()));
            table.addCell(cell);
            
            // Column: Transaction Date Time
            cell.setPhrase(new Phrase(item.transactionDateTime(), getFontContent()));
            table.addCell(cell);
        }
    }

    public void exportToPDF(HttpServletResponse response, Object data) throws IOException {
        // Initialize response for PDF export with appropriate filename
        response = initResponseForExportPdf(response, "InventoryTransactionPdf");
        
        // Define paper size and initialize PDF document
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        
        // Open document
        document.open();
        
        // Title
        Paragraph title = new Paragraph("Report Inventory Transaction", getFontTitle());
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        
        // Subtitle with current report date (could be dynamic)
        Paragraph subtitle = new Paragraph("Report Date: " + java.time.LocalDate.now(), getFontSubtitle());
        subtitle.setAlignment(Paragraph.ALIGN_LEFT);
        document.add(subtitle);
        
        // Add space between subtitle and table
        enterSpace(document);
        
        // Define custom table header matching InventoryTransactionDto structure
        String[] headers = new String[]{
            "S/No.",
            "Transaction ID", 
            "SKU",
            "Description",
            "Quantity",
            "Source",
            "Destination",
            "Transaction Date Time"
        };
        
        PdfPTable tableHeader = new PdfPTable(headers.length);
        writeTableHeaderPdf(tableHeader, headers);
        document.add(tableHeader);
        
        // Create table for data with the same number of columns
        PdfPTable tableData = new PdfPTable(headers.length);
        writeTableData(tableData, data);
        document.add(tableData);
        
        // Close document
        document.close();
    }
}
