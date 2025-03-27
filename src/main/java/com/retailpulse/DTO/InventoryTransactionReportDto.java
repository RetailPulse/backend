package com.retailpulse.DTO;

import java.util.List;

public record InventoryTransactionReportDto(List<InventoryTransactionDto> elements, Long totalElements) {
}
