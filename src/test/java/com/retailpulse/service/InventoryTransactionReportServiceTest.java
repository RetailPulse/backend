package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.entity.Product;
import com.retailpulse.repository.InventoryTransactionRepository;
import com.retailpulse.service.report.InventoryTransactionReportService;
import com.retailpulse.util.DateUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionReportServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @InjectMocks
    private InventoryTransactionReportService inventoryTransactionReportService;

    private Product product;
    private BusinessEntity source;
    private BusinessEntity destination;
    private InventoryTransaction transaction;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("sku123");
        product.setDescription("Sample Description");

        source = new BusinessEntity();
        source.setId(101L);
        source.setName("Source A");

        destination = new BusinessEntity();
        destination.setId(201L);
        destination.setName("Destination B");

        transaction = new InventoryTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setProductId(product.getId());
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);
        transaction.setSource(source.getId());
        transaction.setDestination(destination.getId());
        transaction.setInsertedAt(DateUtil.convertStringToInstant("01-03-2025 00:00", "dd-MM-yyyy HH:mm"));
    }

    @Test
    void getInventoryTransactions_shouldMapEntitiesToDtoCorrectly() {
        var dto = new com.retailpulse.DTO.InventoryTransactionDetailsDto(transaction, product, source, destination);
        when(inventoryTransactionRepository.findAllWithProductAndBusinessEntity(any(), any()))
                .thenReturn(List.of(dto));

        List<InventoryTransactionDto> result = inventoryTransactionReportService.getInventoryTransactions(
                DateUtil.convertStringToInstant("01-03-2025 00:00", "dd-MM-yyyy HH:mm"),
                DateUtil.convertStringToInstant("02-03-2025 00:00", "dd-MM-yyyy HH:mm")
        );

        assertThat(result).hasSize(1);
        InventoryTransactionDto dtoResult = result.get(0);
        assertThat(dtoResult.transactionDateTime()).isEqualTo("01-03-2025 00:00:00");
        assertThat(dtoResult.product().sku()).isEqualTo("sku123");
        assertThat(dtoResult.productPricing().totalCost()).isEqualTo(50.0);
        assertThat(dtoResult.source().name()).isEqualTo("Source A");
        assertThat(dtoResult.destination().name()).isEqualTo("Destination B");
    }

    @Test
    void testExportReport_PdfFormat() throws IOException {
        InventoryTransactionRepository mockRepo = mock(InventoryTransactionRepository.class);
        InventoryTransactionReportService service = new InventoryTransactionReportService(mockRepo);

        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();

        when(mockRepo.findAllWithProductAndBusinessEntity(any(), any())).thenReturn(List.of());

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);

        assertDoesNotThrow(() -> service.exportReport(response, start, end, "pdf"));
    }

    @Test
    void testExportReport_ExcelFormat() throws IOException {
        InventoryTransactionRepository mockRepo = mock(InventoryTransactionRepository.class);
        InventoryTransactionReportService service = new InventoryTransactionReportService(mockRepo);

        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();

        when(mockRepo.findAllWithProductAndBusinessEntity(any(), any())).thenReturn(List.of());

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);

        assertDoesNotThrow(() -> service.exportReport(response, start, end, "excel"));
    }

    @Test
    void testExportReport_InvalidFormat_ThrowsException() {
        InventoryTransactionRepository mockRepo = mock(InventoryTransactionRepository.class);
        InventoryTransactionReportService service = new InventoryTransactionReportService(mockRepo);

        HttpServletResponse response = mock(HttpServletResponse.class);
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> service.exportReport(response, start, end, "csv"));

        assertEquals("Unsupported export format: csv", ex.getMessage());
    }
}
