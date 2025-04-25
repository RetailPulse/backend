package com.retailpulse.service.dataExtractor;

import com.retailpulse.DTO.InventoryTransactionDto;

public class InventoryTransactionDataExtractor implements TableDataExtractor<InventoryTransactionDto> {
    @Override
    public Object[] getRowData(InventoryTransactionDto item, int serialNumber) {
        return new Object[]{
                serialNumber,
                item.transactionId(),
                item.product() != null ? item.product().sku() : "",
                item.product() != null ? item.product().description() : "",
                item.productPricing() != null ? item.productPricing().quantity() : "",
                item.source() != null ? item.source().name() : "",
                item.destination() != null ? item.destination().name() : "",
                item.transactionDateTime()
        };
    }
}