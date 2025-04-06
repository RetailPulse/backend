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
        SalesDetailsDto salesDetailsDto1 = new SalesDetailsDto(1L, 2, "50.0");
        SalesDetailsDto salesDetailsDto2 = new SalesDetailsDto(2L, 3, "100.0");
        SalesDetailsDto salesDetailsDto3 = new SalesDetailsDto(3L, 4, "200.0");
        List<SalesDetailsDto> salesDetailsDtos1 = List.of(salesDetailsDto1, salesDetailsDto2, salesDetailsDto3);
        List<SalesDetailsDto> salesDetailsDtos2 = List.of(salesDetailsDto1, salesDetailsDto2);
        List<SalesDetailsDto> salesDetailsDtos3 = List.of(salesDetailsDto1);

        SuspendedTransactionDto suspendedTransactionDto1 = new SuspendedTransactionDto(1L, salesDetailsDtos1);
        SuspendedTransactionDto suspendedTransactionDto2 = new SuspendedTransactionDto(1L, salesDetailsDtos2);
        SuspendedTransactionDto suspendedTransactionDto3 = new SuspendedTransactionDto(1L, salesDetailsDtos3);

        when(salesTaxRepository.save(any(SalesTax.class))).thenReturn(new SalesTax(TaxType.GST, new BigDecimal("0.09")));

        SalesTransactionHistory salesTransactionHistory = new SalesTransactionHistory();
        SalesTransactionService salesTransactionService = new SalesTransactionService(salesTransactionRepository, salesTaxRepository, salesTransactionHistory, stockUpdateService);

        salesTransactionService.suspendTransaction(suspendedTransactionDto1);
        salesTransactionService.suspendTransaction(suspendedTransactionDto2);
        salesTransactionService.suspendTransaction(suspendedTransactionDto3);

        List<TransientSalesTransactionDto> suspendedTransactions = salesTransactionService.getSuspendedTransactions(1L);

        assertEquals(3, suspendedTransactions.size());
        assertEquals("1200.00", suspendedTransactions.get(0).subTotalAmount());
        assertEquals("108.00", suspendedTransactions.get(0).taxAmount());
        assertEquals("1308.00", suspendedTransactions.get(0).totalAmount());
    }


}
