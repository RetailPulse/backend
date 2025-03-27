package com.retailpulse.DTO;

public record ProductDto(String sku,
                         String description,
                         String category,
                         String subcategory,
                         String brand) {
}
