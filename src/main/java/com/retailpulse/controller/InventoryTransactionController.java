package com.retailpulse.controller;

import com.retailpulse.DTO.InventoryTransactionProductDto;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.service.InventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/inventoryTransaction")
public class InventoryTransactionController {
    private static final Logger logger = Logger.getLogger(InventoryTransactionController.class.getName());

    @Autowired
    InventoryTransactionService inventoryTransactionService;

    @GetMapping
    public ResponseEntity<List<InventoryTransactionProductDto>> getAllInventoryTransactionWithProduct() {
        logger.info("Fetching all inventory transactions");
        return ResponseEntity.ok(inventoryTransactionService.getAllInventoryTransactionWithProduct());
    }

    @PostMapping
    public ResponseEntity<InventoryTransaction> createInventoryTransaction(@RequestBody InventoryTransaction inventoryTransaction) {
        // inventoryTransaction.getQuantity() will always be positive
        logger.info("Received request to create inventoryTransaction: " + inventoryTransaction);
        try {
            InventoryTransaction createdInventoryTransaction = inventoryTransactionService.saveInventoryTransaction(inventoryTransaction);
            logger.info("Successfully created createdInventoryTransaction: " + createdInventoryTransaction);
            return ResponseEntity.ok(createdInventoryTransaction);
        } catch (Exception e) {
            logger.severe("Error creating createdInventoryTransaction: " + e.getMessage());
            throw e;
        }
    }

//    @PutMapping("/{id}")
//    public InventoryTransaction updateInventoryTransaction(@PathVariable UUID id, @RequestBody InventoryTransaction inventoryTransaction) {
//        logger.info("Received request to update inventory transaction with id: " + id);
//        try {
//            InventoryTransaction updatedInventoryTransaction = inventoryTransactionService.updateInventoryTransaction(id, inventoryTransaction);
//            logger.info("Successfully updated inventory transaction with id: " + updatedInventoryTransaction.getId());
//            return updatedInventoryTransaction;
//        } catch (Exception e) {
//            logger.severe("Error updating inventory transaction: " + e.getMessage());
//            throw e;
//        }
//    }
}
