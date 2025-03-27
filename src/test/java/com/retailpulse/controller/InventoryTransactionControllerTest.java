package com.retailpulse.controller;

import com.retailpulse.DTO.InventoryTransactionProductDto;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.entity.Product;
import com.retailpulse.service.InventoryTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryTransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryTransactionService mockInventoryTransactionService;

    @InjectMocks
    private InventoryTransactionController inventoryTransactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryTransactionController).build();
    }

    @Test
    void testGetAllInventoryTransactionWithProduct() throws Exception {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setDescription("Product A");

        InventoryTransaction inventoryTransaction = new InventoryTransaction();
        inventoryTransaction.setId(UUID.randomUUID());
        inventoryTransaction.setProductId(1L);
        inventoryTransaction.setSource(101L);
        inventoryTransaction.setDestination(201L);
        inventoryTransaction.setQuantity(10);
        inventoryTransaction.setCostPricePerUnit(5.0);

        InventoryTransactionProductDto dto1 = new InventoryTransactionProductDto(inventoryTransaction, product);

        List<InventoryTransactionProductDto> mockDtos = Collections.singletonList(dto1);

        when(mockInventoryTransactionService.getAllInventoryTransactionWithProduct()).thenReturn(mockDtos);

        // Act & Assert
        mockMvc.perform(get("/api/inventoryTransaction"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].inventoryTransaction.productId").value(1L))
                .andExpect(jsonPath("$[0].inventoryTransaction.source").value(101L))
                .andExpect(jsonPath("$[0].inventoryTransaction.destination").value(201L))
                .andExpect(jsonPath("$[0].inventoryTransaction.quantity").value(10))
                .andExpect(jsonPath("$[0].inventoryTransaction.costPricePerUnit").value(5.0))
                .andExpect(jsonPath("$[0].product.id").value(1L))
                .andExpect(jsonPath("$[0].product.description").value("Product A"));

        verify(mockInventoryTransactionService, times(1)).getAllInventoryTransactionWithProduct();
        verifyNoMoreInteractions(mockInventoryTransactionService);
    }

    @Test
    void testCreateInventoryTransaction_Successful() throws Exception {
        // Arrange
        InventoryTransaction requestTransaction = new InventoryTransaction();
        requestTransaction.setProductId(1L);
        requestTransaction.setSource(101L);
        requestTransaction.setDestination(201L);
        requestTransaction.setQuantity(10);
        requestTransaction.setCostPricePerUnit(5.0);

        InventoryTransaction responseTransaction = new InventoryTransaction();
        responseTransaction.setId(UUID.randomUUID());
        responseTransaction.setProductId(1L);
        responseTransaction.setSource(101L);
        responseTransaction.setDestination(201L);
        responseTransaction.setQuantity(10);
        responseTransaction.setCostPricePerUnit(5.0);

        when(mockInventoryTransactionService.saveInventoryTransaction(any(InventoryTransaction.class)))
                .thenReturn(responseTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/inventoryTransaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": 1,
                                    "source": 101,
                                    "destination": 201,
                                    "quantity": 10,
                                    "costPricePerUnit": 5.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.source").value(101))
                .andExpect(jsonPath("$.destination").value(201))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.costPricePerUnit").value(5.0));

        verify(mockInventoryTransactionService, times(1))
                .saveInventoryTransaction(any(InventoryTransaction.class));
        verifyNoMoreInteractions(mockInventoryTransactionService);
    }
}