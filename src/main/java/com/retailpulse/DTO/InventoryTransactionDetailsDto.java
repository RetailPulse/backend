package com.retailpulse.DTO;

import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.entity.Product;

public record InventoryTransactionDetailsDto(InventoryTransaction inventoryTransaction, Product product,
                                             BusinessEntity source, BusinessEntity destination) {
}
