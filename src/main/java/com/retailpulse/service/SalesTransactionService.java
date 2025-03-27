package com.retailpulse.service;

import com.retailpulse.DTO.SalesTransactionDetailsDto;
import com.retailpulse.entity.Inventory;
import com.retailpulse.entity.SalesDetails;
import com.retailpulse.entity.SalesTransaction;
import com.retailpulse.repository.SalesDetailsRepository;
import com.retailpulse.repository.SalesTransactionRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SalesTransactionService {

    @Autowired
    private SalesTransactionRepository salesTransactionRepository;

    @Autowired
    private SalesDetailsRepository salesDetailsRepository;

    @Autowired
    private InventoryService inventoryService;

    // Existing methods like createSalesTransaction, updateSalesTransaction, etc.

    /**
     * Calculates the sales tax for the provided list of SalesDetails.
     *
     * @param salesDetails the list of SalesDetails for which to calculate sales tax
     * @return the calculated sales tax value
     */
    public Double calculateSalesTax(List<SalesDetails> salesDetails) {
        // Delegates to the static method in SalesTransaction that performs the calculation.
        return SalesTransaction.calculateSalesTaxBySalesDetails(salesDetails);
    }

    public Optional<SalesTransaction> getSalesTransaction(Long id) {
        return salesTransactionRepository.findById(id);
    }

    /**
     * Retrieves a SalesTransaction along with its associated SalesDetails.
     * This method builds a DTO that contains both the SalesTransaction and its SalesDetails records.
     *
     * @param transactionId the ID of the SalesTransaction to retrieve
     * @return a SalesTransactionDTO containing the transaction and its details
     * @throws IllegalArgumentException if the transaction is not found
     */
    public Optional<SalesTransactionDetailsDto> getFullTransaction(Long transactionId) {
        return salesTransactionRepository.findById(transactionId)
            .map(transaction -> {
                List<SalesDetails> details = salesDetailsRepository.findBySaleId(transactionId);
                return new SalesTransactionDetailsDto(transaction, details);
            });
    }
    

    /**
     * Creates a new SalesTransaction and persists each SalesDetails record.
     * Before creating the transaction, it checks if each product has sufficient
     * available inventory for the shop (businessEntityId). If a product does not have
     * enough quantity, an IllegalArgumentException is thrown.
     *
     * @param businessEntityId the ID of the business entity (shop) making the sale
     * @param salesDetails a list of SalesDetails to be saved as individual rows
     * @return the persisted SalesTransaction, with calculated sales tax and total
     */
    @Transactional
    public SalesTransaction createSalesTransaction(Long businessEntityId, @NotNull List<SalesDetails> salesDetails) {
        // Check available inventory for each sales detail
        for (SalesDetails detail : salesDetails) {
            Optional<Inventory> inventoryOpt = inventoryService.getInventoryByProductIdAndBusinessEntityId(detail.getProductId(), businessEntityId);
            if (inventoryOpt.isEmpty() || inventoryOpt.get().getQuantity() < detail.getQuantity()) {
                throw new IllegalArgumentException("Insufficient inventory for product id: " + detail.getProductId());
            }
        }

        // Calculate subtotal from sales details
        double subtotal = salesDetails.stream()
                .mapToDouble(detail -> detail.getSalesPricePerUnit() * detail.getQuantity())
                .sum();

        // Create a sales transaction with the provided businessEntityId and computed subtotal
        SalesTransaction transaction = new SalesTransaction();
        transaction.setBusinessEntityId(businessEntityId);
        transaction.setSubtotal(subtotal);
        // When saving, the entity lifecycle (@PrePersist) will calculate the sales tax and total.
        transaction = salesTransactionRepository.save(transaction);

        // For each SalesDetails entry, deduct inventory and persist the sales detail
        for (SalesDetails detail : salesDetails) {
            // Deduct sold quantity from the shop's inventory
            Inventory inventory = inventoryService.getInventoryByProductIdAndBusinessEntityId(detail.getProductId(), businessEntityId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product id: " + detail.getProductId()));
            inventory.setQuantity(inventory.getQuantity() - detail.getQuantity());
            inventoryService.updateInventory(inventory.getId(), inventory);

            // Set the saleId for the sales detail and persist it
            detail.setSaleId(transaction.getId());
            salesDetailsRepository.save(detail);
        }
        return transaction;
    }

    /**
     * Updates an existing SalesTransaction by replacing its SalesDetails with new ones.
     * This method reverses the inventory deduction of the old sales details and applies the new ones.
     * If any inventory update fails, the transaction is rolled back.
     *
     * @param transactionId the ID of the SalesTransaction to update
     * @param newSalesDetails a list of new SalesDetails to replace the old ones
     * @return the updated SalesTransaction
     * @throws IllegalArgumentException if the transaction is not found or if inventory is insufficient
     */
    @Transactional
    public SalesTransaction updateSalesTransaction(Long transactionId, @NotNull List<SalesDetails> newSalesDetails) {
        // Retrieve the existing transaction
        SalesTransaction existingTransaction = salesTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Sales transaction not found for id: " + transactionId));

        // Retrieve existing sales details for the transaction
        List<SalesDetails> oldSalesDetails = salesDetailsRepository.findBySaleId(transactionId);

        // Reverse inventory deduction for each old sales detail
        for (SalesDetails oldDetail : oldSalesDetails) {
            Inventory inventory = inventoryService.getInventoryByProductIdAndBusinessEntityId(oldDetail.getProductId(), existingTransaction.getBusinessEntityId())
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product id: " + oldDetail.getProductId()));
            // Add back the quantity that was previously deducted
            inventory.setQuantity(inventory.getQuantity() + oldDetail.getQuantity());
            inventoryService.updateInventory(inventory.getId(), inventory);
        }

        // Remove the old sales details
        salesDetailsRepository.deleteBySaleId(transactionId);

        // Process new sales details: check inventory, deduct quantities, and calculate new subtotal
        double newSubtotal = 0.0;
        for (SalesDetails newDetail : newSalesDetails) {
            Optional<Inventory> inventoryOpt = inventoryService.getInventoryByProductIdAndBusinessEntityId(newDetail.getProductId(), existingTransaction.getBusinessEntityId());
            if (inventoryOpt.isEmpty() || inventoryOpt.get().getQuantity() < newDetail.getQuantity()) {
                throw new IllegalArgumentException("Insufficient inventory for product id: " + newDetail.getProductId());
            }
            Inventory inventory = inventoryOpt.get();
            // Deduct the new quantity from inventory
            inventory.setQuantity(inventory.getQuantity() - newDetail.getQuantity());
            inventoryService.updateInventory(inventory.getId(), inventory);

            // Accumulate the new subtotal
            newSubtotal += newDetail.getSalesPricePerUnit() * newDetail.getQuantity();

            // Associate the new detail with the transaction
            newDetail.setSaleId(transactionId);
            salesDetailsRepository.save(newDetail);
        }

        // Update the transaction's subtotal and update the existing salesTax manually
        existingTransaction.setSubtotal(newSubtotal);
        // Simply saving will trigger calculateSalesTaxAndTotal() so that salesTax is updated.
        salesTransactionRepository.saveAndFlush(existingTransaction);

        return existingTransaction;
    }

}
