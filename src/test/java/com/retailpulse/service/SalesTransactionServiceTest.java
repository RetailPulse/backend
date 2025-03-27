package com.retailpulse.service;

import com.retailpulse.DTO.SalesTransactionDetailsDto;
import com.retailpulse.entity.Inventory;
import com.retailpulse.entity.SalesDetails;
import com.retailpulse.entity.SalesTransaction;
import com.retailpulse.repository.SalesDetailsRepository;
import com.retailpulse.repository.SalesTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesTransactionServiceTest {

    @Mock
    private SalesTransactionRepository salesTransactionRepository;

    @Mock
    private SalesDetailsRepository salesDetailsRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private SalesTransactionService salesTransactionService;

    private SalesTransaction dummyTransaction;
    private SalesDetails dummyDetail;
    private Inventory dummyInventory;

    @BeforeEach
    public void setUp() {
        dummyTransaction = new SalesTransaction();
        dummyTransaction.setId(1L);
        dummyTransaction.setBusinessEntityId(100L);
        dummyTransaction.setSubtotal(200.0);

        dummyDetail = new SalesDetails();
        dummyDetail.setProductId(10L);
        dummyDetail.setQuantity(2);
        dummyDetail.setSalesPricePerUnit(50.0);

        dummyInventory = new Inventory();
        dummyInventory.setId(5L);
        dummyInventory.setProductId(10L);
        dummyInventory.setQuantity(10);
    }

    @Test
    public void testGetSalesTransaction() {
        when(salesTransactionRepository.findById(1L)).thenReturn(Optional.of(dummyTransaction));

        Optional<SalesTransaction> result = salesTransactionService.getSalesTransaction(1L);
        assertTrue(result.isPresent());
        assertEquals(dummyTransaction, result.get());
        verify(salesTransactionRepository, times(1)).findById(1L);
    }

    // @Test
    // public void testGetFullTransaction() {
    //     when(salesTransactionRepository.findById(1L)).thenReturn(Optional.of(dummyTransaction));
    //     when(salesDetailsRepository.findBySaleId(1L)).thenReturn(Collections.singletonList(dummyDetail));

    //     Optional<SalesTransactionDetailsDto> result = salesTransactionService.getFullTransaction(1L);
    //     assertTrue(result.isPresent());
        
    //     SalesTransactionDetailsDto dto = result.get();
    //     assertNotNull(dto.getSalesTransaction());
    //     assertNotNull(dto.getSalesDetails());
        
    //     assertEquals(dummyTransaction, dto.getSalesTransaction());
    //     assertEquals(1, dto.getSalesDetails().size());
    //     assertEquals(dummyDetail, dto.getSalesDetails().get(0));
        
    //     verify(salesTransactionRepository, times(1)).findById(1L);
    //     verify(salesDetailsRepository, times(1)).findBySaleId(1L);
    // }

    @Test
    public void testCreateSalesTransaction_Success() {
        List<SalesDetails> details = Collections.singletonList(dummyDetail);
        // Save the current inventory quantity for later verification.
        int initialInventoryQuantity = dummyInventory.getQuantity();
        // Setup inventory check: this will be called twice by the service and return the same inventory.
        when(inventoryService.getInventoryByProductIdAndBusinessEntityId(dummyDetail.getProductId(), 100L))
                .thenReturn(Optional.of(dummyInventory));
        // Simulate repository save for transaction returning an id.
        SalesTransaction savedTransaction = new SalesTransaction();
        savedTransaction.setId(1L);
        savedTransaction.setBusinessEntityId(100L);
        savedTransaction.setSubtotal(100.0);
        when(salesTransactionRepository.save(any(SalesTransaction.class))).thenReturn(savedTransaction);
        // For sales details save.
        when(salesDetailsRepository.save(any(SalesDetails.class))).thenReturn(dummyDetail);

        SalesTransaction result = salesTransactionService.createSalesTransaction(100L, details);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        // Verify inventory deduction: quantity decreases by sales detail quantity (2).
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryService, times(1)).updateInventory(eq(dummyInventory.getId()), inventoryCaptor.capture());
        assertEquals(initialInventoryQuantity - dummyDetail.getQuantity(), inventoryCaptor.getValue().getQuantity());
        verify(salesDetailsRepository, times(1)).save(any(SalesDetails.class));
    }

    @Test
    public void testCreateSalesTransaction_InsufficientInventory() {
        List<SalesDetails> details = Collections.singletonList(dummyDetail);
        // Setup inventory check: return inventory with insufficient quantity.
        Inventory insufficientInventory = new Inventory();
        insufficientInventory.setId(5L);
        insufficientInventory.setProductId(10L);
        insufficientInventory.setQuantity(1); // less than required 2
        when(inventoryService.getInventoryByProductIdAndBusinessEntityId(dummyDetail.getProductId(), 100L))
                .thenReturn(Optional.of(insufficientInventory));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> salesTransactionService.createSalesTransaction(100L, details));
        assertTrue(exception.getMessage().contains("Insufficient inventory for product id"));
    }

    @Test
    public void testUpdateSalesTransaction_Success() {
        // Prepare old sales detail.
        SalesDetails oldDetail = new SalesDetails();
        oldDetail.setProductId(10L);
        oldDetail.setQuantity(1);
        oldDetail.setSalesPricePerUnit(50.0);

        List<SalesDetails> oldDetails = Collections.singletonList(oldDetail);
        List<SalesDetails> newDetails = Collections.singletonList(dummyDetail);

        // Setup existing transaction.
        when(salesTransactionRepository.findById(1L)).thenReturn(Optional.of(dummyTransaction));
        when(salesDetailsRepository.findBySaleId(1L)).thenReturn(oldDetails);
        // Old inventory reversal: return inventory with product available.
        when(inventoryService.getInventoryByProductIdAndBusinessEntityId(oldDetail.getProductId(), dummyTransaction.getBusinessEntityId()))
                .thenReturn(Optional.of(dummyInventory));
        // For new sales detail: check inventory sufficiency.
        when(inventoryService.getInventoryByProductIdAndBusinessEntityId(dummyDetail.getProductId(), dummyTransaction.getBusinessEntityId()))
                .thenReturn(Optional.of(dummyInventory));
        // For saving sales details.
        when(salesDetailsRepository.save(any(SalesDetails.class))).thenReturn(dummyDetail);
        // For repository save and flush.
        when(salesTransactionRepository.saveAndFlush(any(SalesTransaction.class))).thenReturn(dummyTransaction);

        SalesTransaction updated = salesTransactionService.updateSalesTransaction(1L, newDetails);
        assertNotNull(updated);
        // Verify that old details were deleted.
        verify(salesDetailsRepository, times(1)).deleteBySaleId(1L);
        // Verify inventory update was called for reversal and deduction.
        verify(inventoryService, atLeast(2)).updateInventory(anyLong(), any(Inventory.class));
        // Verify updated subtotal reflects new detail calculation (2 * 50.0 = 100.0).
        assertEquals(100.0, dummyTransaction.getSubtotal());
    }

    @Test
    public void testUpdateSalesTransaction_InsufficientInventory() {
        List<SalesDetails> newDetails = Collections.singletonList(dummyDetail);
        when(salesTransactionRepository.findById(1L)).thenReturn(Optional.of(dummyTransaction));
        when(salesDetailsRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        // Setup inventory check fails for new detail.
        Inventory insufficientInventory = new Inventory();
        insufficientInventory.setId(5L);
        insufficientInventory.setProductId(10L);
        insufficientInventory.setQuantity(1);
        when(inventoryService.getInventoryByProductIdAndBusinessEntityId(dummyDetail.getProductId(), dummyTransaction.getBusinessEntityId()))
                .thenReturn(Optional.of(insufficientInventory));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> salesTransactionService.updateSalesTransaction(1L, newDetails));
        assertTrue(exception.getMessage().contains("Insufficient inventory for product id"));
    }

//    @Test
//    public void testCalculateSalesTax() {
//        // Prepare a list with dummy sales details.
//        List<SalesDetails> salesDetailsList = Collections.singletonList(dummyDetail);
//        double expectedTax = 10.0; // expected tax value
//
//        // Use Mockito's static mocking to control the static call in SalesTransaction.
//        try (MockedStatic<SalesTransaction> mockedStatic = mockStatic(SalesTransaction.class)) {
//            mockedStatic.when(() -> SalesTransaction.calculateSalesTaxBySalesDetails(salesDetailsList))
//                        .thenReturn(expectedTax);
//
//            Double tax = salesTransactionService.calculateSalesTax(salesDetailsList);
//            assertEquals(expectedTax, tax);
//        }
//    }
}
