package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public abstract class AbstractReportExportService {
    public final void exportReport(HttpServletResponse response, Instant start, Instant end, List<InventoryTransactionDto> data) throws IOException {
        initResponse(response);
        writeReportHeader(response, start, end);
        writeTableHeader(response);
        writeTableData(response, data);
        finalizeReport(response);
    }

    protected abstract void initResponse(HttpServletResponse response);
    protected abstract void writeReportHeader(HttpServletResponse response, Instant start, Instant end) throws IOException;
    protected abstract void writeTableHeader(HttpServletResponse response) throws IOException;
    protected abstract void writeTableData(HttpServletResponse response, List<InventoryTransactionDto> data) throws IOException;
    protected abstract void finalizeReport(HttpServletResponse response) throws IOException;
}
