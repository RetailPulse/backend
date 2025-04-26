package com.retailpulse.service.dataExtractor;

import com.retailpulse.DTO.ProductDto;

public class ProductDataExtractor implements TableDataExtractor<ProductDto> {
    @Override
    public Object[] getRowData(ProductDto item, int serialNumber) {
        return new Object[]{
                serialNumber,
                item.sku(),
                item.description(),
                item.category(),
                item.subcategory(),
                item.brand()
        };
    }
}
