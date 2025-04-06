package com.retailpulse.service;

import com.retailpulse.entity.*;
import com.retailpulse.exception.ErrorCodes;
import com.retailpulse.repository.InventoryRepository;
import com.retailpulse.service.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockUpdateServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private StockUpdateService stockUpdateService;

    SalesTransaction dummySalesTransaction;

    @BeforeEach
    public void setUp() {
        // Initialize any necessary objects or mocks here
        SalesDetails salesDetails1 = new SalesDetails(1L, 2, new BigDecimal("50.0"));
        SalesDetails salesDetails2 = new SalesDetails(2L, 3, new BigDecimal("100.0"));
        SalesDetails salesDetails3 = new SalesDetails(3L, 4, new BigDecimal("200.0"));
        List<SalesDetails> salesDetailsList = List.of(salesDetails1, salesDetails2, salesDetails3);

        dummySalesTransaction = new SalesTransaction(1L, new SalesTax(TaxType.GST, new BigDecimal("0.09")));
        salesDetailsList.forEach(dummySalesTransaction::addSalesDetails);
    }

    @Test
    public void testDeductStock() {
        Inventory dummyInventory = new Inventory();
        dummyInventory.setQuantity(10);

        when(inventoryRepository.findByProductIdAndBusinessEntityId(anyLong(), anyLong())).thenReturn(Optional.of(dummyInventory));

        stockUpdateService.deductStock(dummySalesTransaction);

        // Verify that the inventory quantity was updated correctly
        assertEquals(10 - dummySalesTransaction.getSalesDetailEntities().stream().mapToInt(SalesDetails::getQuantity).sum(), dummyInventory.getQuantity());
        verify(inventoryRepository, times(3)).save(dummyInventory);
    }

    @Test
    public void testAddStock() {
        Inventory dummyInventory = new Inventory();
        dummyInventory.setQuantity(10);

        when(inventoryRepository.findByProductIdAndBusinessEntityId(anyLong(), anyLong())).thenReturn(Optional.of(dummyInventory));

        stockUpdateService.addStock(dummySalesTransaction);

        // Verify that the inventory quantity was updated correctly
        assertEquals(10 + dummySalesTransaction.getSalesDetailEntities().stream().mapToInt(SalesDetails::getQuantity).sum(), dummyInventory.getQuantity());
        verify(inventoryRepository, times(3)).save(dummyInventory);
    }

    @Test
    public void testInsufficientStockWhenDeductStock() {
        Inventory dummyInventory = new Inventory();
        dummyInventory.setQuantity(1);

        when(inventoryRepository.findByProductIdAndBusinessEntityId(anyLong(), anyLong())).thenReturn(Optional.of(dummyInventory));

        // Expect an exception to be thrown
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            stockUpdateService.deductStock(dummySalesTransaction);
        });

        assertEquals("Insufficient stock for product id: 1", exception.getMessage());
        assertEquals(ErrorCodes.INSUFFICIENT_INVENTORY, exception.getErrorCode());
    }

    @Test
    public void testNotFoundInventoryWhenDeductStock() {
        when(inventoryRepository.findByProductIdAndBusinessEntityId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Expect an exception to be thrown
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            stockUpdateService.deductStock(dummySalesTransaction);
        });

        assertEquals("Inventory not found for product id: 1", exception.getMessage());
        assertEquals(ErrorCodes.NOT_FOUND, exception.getErrorCode());
    }
}