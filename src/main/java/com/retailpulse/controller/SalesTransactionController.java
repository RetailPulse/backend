package com.retailpulse.controller;

import com.retailpulse.DTO.SalesTransactionDetailsDto;
import com.retailpulse.entity.SalesDetails;
import com.retailpulse.entity.SalesTransaction;
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

    /**
     * Endpoint to calculate sales tax based on provided SalesDetails.
     *
     * @param salesDetails list of SalesDetails in the request body
     * @return the calculated sales tax value
     */
    @PostMapping("/calculateSalesTax")
    public ResponseEntity<Double> calculateSalesTax(@RequestBody List<SalesDetails> salesDetails) {
        Double tax = salesTransactionService.calculateSalesTax(salesDetails);
        return ResponseEntity.ok(tax);
    }

    /**
     * Endpoint to retrieve a SalesTransaction by its ID.
     * Returns 200 OK if found, or 400 BAD REQUEST if not found.
     *
     * @param id the ID of the SalesTransaction
     * @return the SalesTransaction if found, or 404 if not
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalesTransaction> getSalesTransaction(@PathVariable Long id) {
        return salesTransactionService.getSalesTransaction(id)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    /**
     * Endpoint to retrieve a SalesTransaction along with its SalesDetails packaged as a DTO.
     *
     * @param transactionId the ID of the SalesTransaction
     * @return a DTO containing the SalesTransaction and its details
     */
    @GetMapping("/{transactionId}/full")
    public ResponseEntity<SalesTransactionDetailsDto> getFullTransaction(@PathVariable Long transactionId) {
        return salesTransactionService.getFullTransaction(transactionId)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    /**
     * Endpoint to create a new SalesTransaction.
     *
     * @param businessEntityId the ID of the business entity (shop)
     * @param salesDetails a list of SalesDetails to be associated with the transaction
     * @return the created SalesTransaction
     */
    @PostMapping("/createTransaction/{businessEntityId}")
    public ResponseEntity<SalesTransaction> createSalesTransaction(
            @PathVariable Long businessEntityId,
            @RequestBody List<SalesDetails> salesDetails) {
        SalesTransaction transaction = salesTransactionService.createSalesTransaction(businessEntityId, salesDetails);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    /**
     * Endpoint to update an existing SalesTransaction.
     *
     * @param transactionId the ID of the SalesTransaction to update
     * @param newSalesDetails a list of new SalesDetails to replace the old ones
     * @return the updated SalesTransaction
     */
    @PutMapping("updateSalesTransaction/{transactionId}")
    public ResponseEntity<SalesTransaction> updateSalesTransaction(
            @PathVariable Long transactionId,
            @RequestBody List<SalesDetails> newSalesDetails) {
        SalesTransaction updatedTransaction = salesTransactionService.updateSalesTransaction(transactionId, newSalesDetails);
        return ResponseEntity.ok(updatedTransaction);
    }
}