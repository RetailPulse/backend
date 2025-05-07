package com.retailpulse.service;

import com.retailpulse.DTO.ProductDto;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.entity.Product;
import com.retailpulse.repository.ProductRepository;
import com.retailpulse.service.report.ProductReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductReportServiceTest {

    private ProductRepository productRepository;
    private ProductReportService productReportService;

    @BeforeEach
    void setup() {
        productRepository = mock(ProductRepository.class);
        productReportService = new ProductReportService(productRepository);
    }

    @Test
    void testGetAllProducts() {
        Product product = new Product();
        product.setSku("sku1");
        product.setDescription("desc");
        product.setCategory("cat");
        product.setSubcategory("subcat");
        product.setBrand("brand");

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductDto> result = productReportService.getAllProducts();
        assertEquals(1, result.size());
        assertEquals("sku1", result.get(0).sku());
    }

    @Test
    void testExportReport_withInvalidFormat_shouldThrowException() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                productReportService.exportReport(response, Instant.now(), Instant.now(), "txt")
        );
        assertTrue(ex.getMessage().contains("Unsupported export format"));
    }

    @Test
    void testExportReport_withExcelFormat_shouldCallExport() throws Exception {
        Product product = new Product();
        product.setSku("sku1");
        product.setDescription("desc");
        product.setCategory("cat");
        product.setSubcategory("subcat");
        product.setBrand("brand");

        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream());

        productReportService.exportReport(response, Instant.now(), Instant.now(), "excel");

        verify(productRepository).findAll();
    }

    private static class MockServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final OutputStream delegate = OutputStream.nullOutputStream();
        @Override public void write(int b) {}
        @Override public boolean isReady() { return true; }
        @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
    }
}