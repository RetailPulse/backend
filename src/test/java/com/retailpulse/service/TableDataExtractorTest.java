package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionDetailsDto;
import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.DTO.ProductDto;
import com.retailpulse.DTO.ProductPricingDto;
import com.retailpulse.DTO.BusinessEntityDto;
import com.retailpulse.DTO.mapper.InventoryTransactionReportMapper;
import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.entity.Product;
import com.retailpulse.service.dataExtractor.InventoryTransactionDataExtractor;

import com.retailpulse.service.dataExtractor.ProductDataExtractor;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TableDataExtractorTest {

    @Test
    void testGetRowData_AllFieldsPresent() {
        InventoryTransactionDto dto = new InventoryTransactionDto(
                "txn-001",
                new ProductDto("sku-123", "Product Desc", "Product Cat", "Product SubCat", "Product Brand"),
                new ProductPricingDto(5, 2.0),
                new BusinessEntityDto("Source Name", "Source Location", "Source Type"),
                new BusinessEntityDto("Destination Name", "Destination Location", "Destination Type"),
                "2025-05-06T12:00:00Z"
        );

        InventoryTransactionDataExtractor extractor = new InventoryTransactionDataExtractor();
        Object[] row = extractor.getRowData(dto, 1);

        assertEquals(1, row[0]);
        assertEquals("txn-001", row[1]);
        assertEquals("sku-123", row[2]);
        assertEquals("Product Desc", row[3]);
        assertEquals(5, row[4]);
        assertEquals("Source Name", row[5]);
        assertEquals("Destination Name", row[6]);
        assertEquals("2025-05-06T12:00:00Z", row[7]);
    }

    @Test
    void testGetRowData_withNullFields_shouldHandleGracefully() {
        InventoryTransactionDataExtractor extractor = new InventoryTransactionDataExtractor();

        InventoryTransactionDto dto = new InventoryTransactionDto(
                null,
                null,
                null,
                null,
                null,
                null
        );

        Object[] row = extractor.getRowData(dto, 1);

        assertEquals(1, row[0]);
        assertNull(row[1]); // Transaction ID
        assertEquals("", row[2]); // Product SKU
        assertEquals("", row[3]); // Product Description
        assertEquals("", row[4]); // Quantity
        assertEquals("", row[5]); // Source Name
        assertEquals("", row[6]); // Destination Name
        assertNull(row[7]); // DateTime
    }

    @Test
    void testToInventoryTransactionDto_withValidData_shouldMapCorrectly() {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(12.5);
        transaction.setInsertedAt(Instant.parse("2025-05-06T02:00:00Z"));

        Product product = new Product();
        product.setSku("SKU001");
        product.setDescription("Test Product");
        product.setCategory("Category A");
        product.setSubcategory("Sub A");
        product.setBrand("BrandX");

        BusinessEntity source = new BusinessEntity();
        source.setName("Warehouse A");
        source.setLocation("Location A");
        source.setType("Warehouse");

        BusinessEntity destination = new BusinessEntity();
        destination.setName("Store B");
        destination.setLocation("Location B");
        destination.setType("Store");

        InventoryTransactionDetailsDto dto = new InventoryTransactionDetailsDto(transaction, product, source, destination);

        InventoryTransactionDto result = InventoryTransactionReportMapper.toInventoryTransactionDto(dto);

        assertEquals("SKU001", result.product().sku());
        assertEquals(10, result.productPricing().quantity());
        assertEquals("Warehouse A", result.source().name());
        assertEquals("Store B", result.destination().name());
        assertEquals("06-05-2025 10:00:00", result.transactionDateTime());
    }

    @Test
    void testProductDataExtractor_withValidData_shouldReturnCorrectRow() {
        ProductDataExtractor extractor = new ProductDataExtractor();
        ProductDto dto = new ProductDto(
                "SKU123",
                "Product Description",
                "Category",
                "Subcategory",
                "Brand"
        );

        Object[] rowData = extractor.getRowData(dto, 1);

        assertEquals(1, rowData[0]);
        assertEquals("SKU123", rowData[1]);
        assertEquals("Product Description", rowData[2]);
        assertEquals("Category", rowData[3]);
        assertEquals("Subcategory", rowData[4]);
        assertEquals("Brand", rowData[5]);
    }

    @Test
    void testProductDataExtractor_withNullFields_shouldHandleGracefully() {
        ProductDataExtractor extractor = new ProductDataExtractor();
        ProductDto dto = new ProductDto(
                null,
                null,
                null,
                null,
                null
        );

        Object[] rowData = extractor.getRowData(dto, 1);

        assertEquals(1, rowData[0]);
        assertNull(rowData[1]);
        assertNull(rowData[2]);
        assertNull(rowData[3]);
        assertNull(rowData[4]);
        assertNull(rowData[5]);
    }
}