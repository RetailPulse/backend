package com.retailpulse.DTO;

public record InventoryTransactionDto(String transactionId,
                                      ProductDto product,
                                      ProductPricingDto productPricing,
                                      BusinessEntityDto source,
                                      BusinessEntityDto destination,
                                      String transactionDateTime
                                            ) {
}
