package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionDto;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class InventoryTransactionExportToExcelService extends ReportAbstract {
    public void writeTableData(Object data) {
        List<InventoryTransactionDto> list = (List<InventoryTransactionDto>) data;
        CellStyle style = getFontContentExcel();
        
        int startRow = 2;
        int serialNo = 1;  // Added counter for S/No.
        
        for (InventoryTransactionDto dto : list) {
            Row row = sheet.createRow(startRow++);
            int columnCount = 0;
            
            // Column 1: S/No.
            createCell(row, columnCount++, String.valueOf(serialNo++), style);
            // Column 2: Transaction ID
            createCell(row, columnCount++, dto.transactionId(), style);
            // Column 3: SKU
            String sku = dto.product() != null ? dto.product().sku() : "";
            createCell(row, columnCount++, sku, style);
            // Column 4: Description
            String description = dto.product() != null ? String.valueOf(dto.product().description()) : "";
            createCell(row, columnCount++, description, style);
            // Column 5: Quantity
            String quantity = dto.productPricing() != null ? String.valueOf(dto.productPricing().quantity()) : "";
            createCell(row, columnCount++, quantity, style);
            // Column 6: Source
            String source = dto.source() != null ? dto.source().name() : "";
            createCell(row, columnCount++, source, style);
            // Column 7: Destination
            String destination = dto.destination() != null ? dto.destination().name() : "";
            createCell(row, columnCount++, destination, style);
            // Column 8: Transaction Date Time
            createCell(row, columnCount++, dto.transactionDateTime(), style);
        }
    }

    public void exportToExcel(HttpServletResponse response, Object data) throws IOException {
        newReportExcel();

        // response writer to excel
        response = initResponseForExportExcel(response, "InventoryTransactionExcel");
        ServletOutputStream outputStream = response.getOutputStream();

        // update headers to match InventoryTransactionDto structure
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
        writeTableHeaderExcel("Sheet Inventory Transaction", "Report Inventory Transaction", headers);

        // write content row
        writeTableData(data);

        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
