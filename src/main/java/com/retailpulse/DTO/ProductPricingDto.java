package com.retailpulse.DTO;

public record ProductPricingDto(int quantity,
                               double costPricePerUnit,
                               double totalCost) {

    public  ProductPricingDto {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (costPricePerUnit < 0) {
            throw new IllegalArgumentException("Cost price per unit cannot be negative");
        }
    }

    public ProductPricingDto(int quantity, double costPricePerUnit) {
        this(quantity, costPricePerUnit, costPricePerUnit * quantity);
    }
}
