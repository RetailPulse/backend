package com.retailpulse.service;


import com.retailpulse.DTO.InventoryTransactionDetailsDto;
import com.retailpulse.DTO.InventoryTransactionDto;
import com.retailpulse.DTO.mapper.InventoryTransactionReportMapper;
import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.entity.Product;
import com.retailpulse.repository.InventoryTransactionRepository;
import com.retailpulse.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryTransactionReportServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Test
    public void getInventoryTransactionsTest() {
        // Given
        Product product1 = new Product();
        product1.setId(1L);
        product1.setSku("sku1");
        product1.setDescription("description1");
        product1.setCategory("category1");
        product1.setSubcategory("subcategory1");
        product1.setBrand("brand1");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setSku("sku2");
        product2.setDescription("description2");
        product2.setCategory("category2");
        product2.setSubcategory("subcategory2");
        product2.setBrand("brand2");

        Product product3 = new Product();
        product2.setId(3L);
        product2.setSku("sku3");
        product2.setDescription("description3");
        product2.setCategory("category3");
        product2.setSubcategory("subcategory3");
        product2.setBrand("brand3");

        BusinessEntity source = new BusinessEntity();
        source.setId(1L);
        source.setName("source1");
        source.setLocation("location1");
        source.setType("type1");

        BusinessEntity destination = new BusinessEntity();
        destination.setId(2L);
        destination.setName("dest");
        destination.setLocation("location1");
        destination.setType("type1");

        InventoryTransaction inventoryTransaction1 = new InventoryTransaction();
        inventoryTransaction1.setId(UUID.randomUUID());
        inventoryTransaction1.setProductId(product1.getId());
        inventoryTransaction1.setQuantity(10);
        inventoryTransaction1.setCostPricePerUnit(100.0);
        inventoryTransaction1.setSource(source.getId());
        inventoryTransaction1.setDestination(destination.getId());
        inventoryTransaction1.setInsertedAt(DateUtil.convertStringToInstant("01-03-2025 00:00", "dd-MM-yyyy HH:mm"));

        InventoryTransaction inventoryTransaction2 = new InventoryTransaction();
        inventoryTransaction2.setId(UUID.randomUUID());
        inventoryTransaction2.setProductId(product2.getId());
        inventoryTransaction2.setQuantity(8);
        inventoryTransaction2.setCostPricePerUnit(88.0);
        inventoryTransaction2.setSource(source.getId());
        inventoryTransaction2.setDestination(destination.getId());
        inventoryTransaction2.setInsertedAt(DateUtil.convertStringToInstant("01-03-2025 00:00", "dd-MM-yyyy HH:mm"));

        InventoryTransaction inventoryTransaction3 = new InventoryTransaction();
        inventoryTransaction3.setId(UUID.randomUUID());
        inventoryTransaction3.setProductId(product3.getId());
        inventoryTransaction3.setQuantity(4);
        inventoryTransaction3.setCostPricePerUnit(50.0);
        inventoryTransaction3.setSource(source.getId());
        inventoryTransaction3.setDestination(destination.getId());
        inventoryTransaction3.setInsertedAt(DateUtil.convertStringToInstant("01-03-2025 00:00", "dd-MM-yyyy HH:mm"));

        InventoryTransactionDetailsDto inventoryTransactionDetailsDto1 = new InventoryTransactionDetailsDto(inventoryTransaction1, product1, source, destination);
        InventoryTransactionDetailsDto inventoryTransactionDetailsDto2 = new InventoryTransactionDetailsDto(inventoryTransaction2, product2, source, destination);
        InventoryTransactionDetailsDto inventoryTransactionDetailsDto3 = new InventoryTransactionDetailsDto(inventoryTransaction3, product3, source, destination);

        List<InventoryTransactionDetailsDto> inventoryTransactionDetailsDtos = new ArrayList<>();
        inventoryTransactionDetailsDtos.add(inventoryTransactionDetailsDto1);
        inventoryTransactionDetailsDtos.add(inventoryTransactionDetailsDto2);
        inventoryTransactionDetailsDtos.add(inventoryTransactionDetailsDto3);

        // When
        when(inventoryTransactionRepository.findAllWithProductAndBusinessEntity(any(Instant.class), any(Instant.class))).thenReturn(inventoryTransactionDetailsDtos);

        // Then
        List<InventoryTransactionDetailsDto> result = inventoryTransactionRepository.findAllWithProductAndBusinessEntity(
                DateUtil.convertStringToInstant("01-03-2025 00:00", "dd-MM-yyyy HH:mm"), DateUtil.convertStringToInstant("31-03-2025 00:00", "dd-MM-yyyy HH:mm")
        );

        List<InventoryTransactionDto> inventoryTransactionDtos = result.stream().map(InventoryTransactionReportMapper::toInventoryTransactionDto).collect(Collectors.toList());

        // Assertions
        assertThat(inventoryTransactionDtos.size()).isEqualTo(inventoryTransactionDetailsDtos.size());
        assertThat(inventoryTransactionDtos.get(0).transactionDateTime()).isEqualTo("01-03-2025 00:00:00");
        assertThat(inventoryTransactionDtos.get(0).product().sku()).isEqualTo("sku1");
        assertThat(inventoryTransactionDtos.get(0).productPricing().totalCost()).isEqualTo(1000.0);
        assertThat(inventoryTransactionDtos.get(0).source().name()).isEqualTo("source1");
        assertThat(inventoryTransactionDtos.get(0).destination().name()).isEqualTo("dest");

    }


}
