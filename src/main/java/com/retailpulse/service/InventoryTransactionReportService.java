package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.DTO.mapper.InventoryTransactionReportMapper;
import com.retailpulse.repository.InventoryTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryTransactionReportService {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryTransactionReportService.class);

    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryTransactionReportService(InventoryTransactionRepository inventoryTransactionRepository) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    public List<InventoryTransactionDto> getInventoryTransactions(Instant startDateTime, Instant endDateTime) {
        LOG.info("Fetching all inventory transactions");
        return inventoryTransactionRepository.findAllWithProductAndBusinessEntity(startDateTime, endDateTime)
                .stream()
                .map(InventoryTransactionReportMapper::toInventoryTransactionDto)
                .collect(Collectors.toList());

    }
}
