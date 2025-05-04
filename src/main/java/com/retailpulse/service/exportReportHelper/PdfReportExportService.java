package com.retailpulse.service.exportReportHelper;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.retailpulse.service.dataExtractor.TableDataExtractor;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportExportService<T> extends AbstractReportExportService<T> {

    private final String title;
    private final String[] headers;
    private final TableDataExtractor<T> extractor;
    private Document document;

    // Constructor now accepts a flexible title
    public PdfReportExportService(String title, String[] headers, TableDataExtractor<T> extractor) {
        this.title = title;
        this.headers = headers;
        this.extractor = extractor;
    }

    @Override
    protected void initResponse(HttpServletResponse response) {
        response.setContentType("application/pdf");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String fileName = "report_" + now.format(formatter) + ".pdf";
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

        // Use flexible title here
        Paragraph titleParagraph = new Paragraph(this.title, new Font(Font.HELVETICA, 16, Font.BOLD));
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(titleParagraph);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Always show generated on time
        document.add(new Paragraph("Report Generated On: " + dtf.format(LocalDateTime.now()), new Font(Font.HELVETICA, 12)));

        // Only add date range if both start and end are provided (not null)
        if (start != null && end != null) {
            String startFormatted = dtf.format(LocalDateTime.ofInstant(start, ZoneId.systemDefault()));
            String endFormatted = dtf.format(LocalDateTime.ofInstant(end, ZoneId.systemDefault()));
            document.add(new Paragraph("Start Date: " + startFormatted + "    End Date: " + endFormatted, new Font(Font.HELVETICA, 12)));
        }
        document.add(Chunk.NEWLINE);
    }

    @Override
    protected void writeTableHeader(HttpServletResponse response) throws IOException {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            table.addCell(cell);
        }
        document.add(table);
    }

    @Override
    protected void writeTableData(HttpServletResponse response, List<T> data) throws IOException {
        PdfPTable dataTable = new PdfPTable(headers.length);
        dataTable.setWidthPercentage(100);
        Font dataFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        int serialNumber = 0;
        for (T item : data) {
            serialNumber++;
            Object[] rowData = extractor.getRowData(item, serialNumber);
            for (Object cellData : rowData) {
                PdfPCell cell = new PdfPCell(new Phrase(cellData.toString(), dataFont));
                dataTable.addCell(cell);
            }
        }
        document.add(dataTable);
    }

    @Override
    protected void finalizeReport(HttpServletResponse response) throws IOException {
        document.close();
    }
}
