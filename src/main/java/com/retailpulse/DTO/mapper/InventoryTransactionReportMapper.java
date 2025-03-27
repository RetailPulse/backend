package com.retailpulse.DTO.mapper;

import com.retailpulse.DTO.*;
import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.entity.Product;
import com.retailpulse.util.DateUtil;

public class InventoryTransactionReportMapper {
    private static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    public static InventoryTransactionDto toInventoryTransactionDto(InventoryTransactionDetailsDto detailsDto) {
        InventoryTransaction transaction = detailsDto.inventoryTransaction();
        Product product = detailsDto.product();
        BusinessEntity source = detailsDto.source();
        BusinessEntity destination = detailsDto.destination();

        ProductDto productDto = new ProductDto(
                product.getSku(),
                product.getDescription(),
                product.getCategory(),
                product.getSubcategory(),
                product.getBrand()
        );

        ProductPricingDto productPricingDto = new ProductPricingDto(
                transaction.getQuantity(),
                transaction.getCostPricePerUnit()
        );

        BusinessEntityDto sourceDto = new BusinessEntityDto(
                source.getName(),
                source.getLocation(),
                source.getType()
        );

        BusinessEntityDto destinationDto = new BusinessEntityDto(
                destination.getName(),
                destination.getLocation(),
                destination.getType()
        );

        return new InventoryTransactionDto(
                transaction.getId().toString(),
                productDto,
                productPricingDto,
                sourceDto,
                destinationDto,
                DateUtil.convertInstantToString(transaction.getInsertedAt(), DATE_TIME_FORMAT)
        );

    }

}
