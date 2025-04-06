package com.retailpulse.controller;

import com.retailpulse.controller.request.SalesDetailsDto;
import com.retailpulse.controller.request.SalesTransactionRequestDto;
import com.retailpulse.controller.request.SuspendedTransactionDto;
import com.retailpulse.controller.response.SalesTransactionResponseDto;
import com.retailpulse.controller.response.TransientSalesTransactionDto;
import com.retailpulse.service.SalesTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesTransactionController {

    @Autowired
    private SalesTransactionService salesTransactionService;


    @PostMapping("/{businessEntityId}/calculateSalesTax")
    public ResponseEntity<TransientSalesTransactionDto> calculateSalesTax(@PathVariable Long businessEntityId,
                                                                          @RequestBody List<SalesDetailsDto> salesDetailsDtos) {
        TransientSalesTransactionDto transientSalesTransactionDto = salesTransactionService.calculateSalesTax(businessEntityId, salesDetailsDtos);
        return ResponseEntity.ok(transientSalesTransactionDto);
    }

    /**
     * Endpoint to create a new SalesTransaction.
     *
     * @param requestDto the SalesTransactionRequestDto containing transaction details
     * @return the created SalesTransaction
     */
    @PostMapping("/createTransaction")
    public ResponseEntity<SalesTransactionResponseDto> createSalesTransaction(@RequestBody SalesTransactionRequestDto requestDto) {
        SalesTransactionResponseDto responseDto = salesTransactionService.createSalesTransaction(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /**
     * Endpoint to update an existing SalesTransaction.
     *
     * @param transactionId          the ID of the SalesTransaction to update
     * @param newSalesDetailEntities a list of new SalesDetails to replace the old ones
     * @return the updated SalesTransaction
     */
    @PutMapping("updateSalesTransaction/{transactionId}")
    public ResponseEntity<SalesTransactionResponseDto> updateSalesTransaction(
            @PathVariable Long transactionId,
            @RequestBody List<SalesDetailsDto> newSalesDetailEntities) {
        SalesTransactionResponseDto updatedTransaction = salesTransactionService.updateSalesTransaction(transactionId, newSalesDetailEntities);
        return ResponseEntity.ok(updatedTransaction);
    }

    @PostMapping("/suspend")
    public ResponseEntity<String> suspendTransaction(@RequestBody SuspendedTransactionDto suspendedTransactionDto) {
        salesTransactionService.suspendTransaction(suspendedTransactionDto);
        return ResponseEntity.ok("Transaction suspended successfully.");
    }

    @PostMapping("/{businessEntityId}/suspended-transactions")
    public ResponseEntity<List<TransientSalesTransactionDto>> getSuspendedTransactions(@PathVariable Long businessEntityId) {
        List<TransientSalesTransactionDto> transactionHistory = salesTransactionService.getSuspendedTransactions(businessEntityId);
        return ResponseEntity.ok(transactionHistory);
    }

}