package com.retailpulse.service;

import com.retailpulse.controller.request.SalesDetailsDto;
import com.retailpulse.controller.request.SuspendedTransactionDto;
import com.retailpulse.controller.response.TransientSalesTransactionDto;
import com.retailpulse.entity.SalesTax;
import com.retailpulse.entity.TaxType;
import com.retailpulse.repository.SalesTaxRepository;
import com.retailpulse.repository.SalesTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SalesTransactionMementoTest {

    @Mock
    private SalesTaxRepository salesTaxRepository;

    @Mock
    private SalesTransactionRepository salesTransactionRepository;

    @Mock
    private StockUpdateService stockUpdateService;

    @Test
    public void testSalesTransactionMemento() {
        // Arrange: Prepare test data
        SalesDetailsDto salesDetailsDto1 = new SalesDetailsDto(1L, 2, "50.0");
        SalesDetailsDto salesDetailsDto2 = new SalesDetailsDto(2L, 3, "100.0");
        SalesDetailsDto salesDetailsDto3 = new SalesDetailsDto(3L, 4, "200.0");
        List<SalesDetailsDto> salesDetailsDtos = List.of(salesDetailsDto1, salesDetailsDto2, salesDetailsDto3);

        SuspendedTransactionDto suspendedTransactionDto = new SuspendedTransactionDto(1L, salesDetailsDtos);

        when(salesTaxRepository.save(any(SalesTax.class))).thenReturn(new SalesTax(TaxType.GST, new BigDecimal("0.09")));

        SalesTransactionHistory salesTransactionHistory = new SalesTransactionHistory();
        SalesTransactionService salesTransactionService = new SalesTransactionService(
            salesTransactionRepository, salesTaxRepository, salesTransactionHistory, stockUpdateService
        );

        // Act: Suspend the transaction
        List<TransientSalesTransactionDto> suspendedTransactions = salesTransactionService.suspendTransaction(suspendedTransactionDto);

        // Assert: Verify the suspension behavior
        assertEquals(1, suspendedTransactions.size(), "There should be one suspended transaction");

        // Act: Restore the transaction
        List<TransientSalesTransactionDto> remainingTransactions = salesTransactionService.restoreTransaction(
            1L, suspendedTransactions.get(0).transactionId()
        );

        // Assert: Verify the restoration behavior
        assertEquals(0, remainingTransactions.size(), "All transactions should be restored, leaving none suspended");
    }

}
