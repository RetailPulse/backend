package com.retailpulse.controller;

import com.retailpulse.entity.Inventory;
import com.retailpulse.service.InventoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryControllerTest {

    private MockMvc mockMvc;

    private final InventoryService mockInventoryService = Mockito.mock(InventoryService.class); // Mocked service

    @BeforeEach
    void setUp() {
        InventoryController inventoryController = new InventoryController(mockInventoryService);
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    @Test
    void testGetAllInventories() throws Exception {
        // Create Inventory objects using setters
        Inventory item01 = new Inventory();
        item01.setId(1L);
        item01.setProductId(101L);
        item01.setBusinessEntityId(201L);
        item01.setQuantity(50);

        Inventory item02 = new Inventory();
        item02.setId(2L);
        item02.setProductId(102L);
        item02.setBusinessEntityId(202L);
        item02.setQuantity(30);

        List<Inventory> mockInventories = Arrays.asList(item01, item02);

        when(mockInventoryService.getAllInventory()).thenReturn(mockInventories);

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(mockInventories.size()))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].productId").value(101L))
                .andExpect(jsonPath("$[0].businessEntityId").value(201L))
                .andExpect(jsonPath("$[0].quantity").value(50))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].productId").value(102L))
                .andExpect(jsonPath("$[1].businessEntityId").value(202L))
                .andExpect(jsonPath("$[1].quantity").value(30));

        verify(mockInventoryService, times(1)).getAllInventory();
        verifyNoMoreInteractions(mockInventoryService);
    }

    @Test
    void testGetInventoryById() throws Exception {
        Long inventoryId = 1L;

        Inventory mockInventory = new Inventory();
        mockInventory.setId(inventoryId);
        mockInventory.setProductId(101L);
        mockInventory.setBusinessEntityId(201L);
        mockInventory.setQuantity(50);

        when(mockInventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(mockInventory));

        mockMvc.perform(get("/api/inventory/{id}", inventoryId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(inventoryId))
                .andExpect(jsonPath("$.productId").value(101L))
                .andExpect(jsonPath("$.businessEntityId").value(201L))
                .andExpect(jsonPath("$.quantity").value(50));

        verify(mockInventoryService, times(1)).getInventoryById(inventoryId);
        verifyNoMoreInteractions(mockInventoryService);
    }

    @Test
    void testGetInventoryByProductId() throws Exception {
        Long productId = 101L;

        Inventory mockInventory = new Inventory();
        mockInventory.setId(1L);
        mockInventory.setProductId(productId);
        mockInventory.setBusinessEntityId(201L);
        mockInventory.setQuantity(50);

        List<Inventory> mockInventories = Collections.singletonList(mockInventory);

        when(mockInventoryService.getInventoryByProductId(productId)).thenReturn(mockInventories);

        mockMvc.perform(get("/api/inventory/productId/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(mockInventories.size()))
                .andExpect(jsonPath("$[0].productId").value(productId))
                .andExpect(jsonPath("$[0].businessEntityId").value(201L))
                .andExpect(jsonPath("$[0].quantity").value(50));

        verify(mockInventoryService, times(1)).getInventoryByProductId(productId);
        verifyNoMoreInteractions(mockInventoryService);
    }

    @Test
    void testGetInventoryByBusinessEntityId() throws Exception {
        Long businessEntityId = 201L;

        Inventory mockInventory = new Inventory();
        mockInventory.setId(1L);
        mockInventory.setProductId(101L);
        mockInventory.setBusinessEntityId(businessEntityId);
        mockInventory.setQuantity(50);

        List<Inventory> mockInventories = Collections.singletonList(mockInventory);

        when(mockInventoryService.getInventoryByBusinessEntityId(businessEntityId)).thenReturn(mockInventories);

        mockMvc.perform(get("/api/inventory/businessEntityId/{id}", businessEntityId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(mockInventories.size()))
                .andExpect(jsonPath("$[0].businessEntityId").value(businessEntityId))
                .andExpect(jsonPath("$[0].productId").value(101L))
                .andExpect(jsonPath("$[0].quantity").value(50));

        verify(mockInventoryService, times(1)).getInventoryByBusinessEntityId(businessEntityId);
        verifyNoMoreInteractions(mockInventoryService);
    }

    @Test
    void testGetInventoryByProductIdAndBusinessEntityId() throws Exception {
        Long productId = 101L;
        Long businessEntityId = 201L;

        Inventory mockInventory = new Inventory();
        mockInventory.setId(1L);
        mockInventory.setProductId(productId);
        mockInventory.setBusinessEntityId(businessEntityId);
        mockInventory.setQuantity(50);

        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(productId, businessEntityId))
                .thenReturn(Optional.of(mockInventory));

        mockMvc.perform(get("/api/inventory/productId/{productId}/businessEntityId/{businessEntityId}",
                        productId, businessEntityId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.businessEntityId").value(businessEntityId))
                .andExpect(jsonPath("$.quantity").value(50));

        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(productId, businessEntityId);
        verifyNoMoreInteractions(mockInventoryService);
    }

    @Test
    void testGetInventoryByProductIdAndBusinessEntityIdFound() throws Exception {
        Long productId = 101L;
        Long businessEntityId = 201L;
        
        Inventory mockInventory = new Inventory();
        mockInventory.setId(1L);
        mockInventory.setProductId(productId);
        mockInventory.setBusinessEntityId(businessEntityId);
        mockInventory.setQuantity(50);
        
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(productId, businessEntityId))
                .thenReturn(Optional.of(mockInventory));
        
        mockMvc.perform(get("/api/inventory/productId/{productId}/businessEntityId/{businessEntityId}",
                        productId, businessEntityId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.businessEntityId").value(businessEntityId))
                .andExpect(jsonPath("$.quantity").value(50));
        
        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(productId, businessEntityId);
        verifyNoMoreInteractions(mockInventoryService);
    }

    @Test
    void testGetInventoryByProductIdAndBusinessEntityIdNotFound() throws Exception {
        Long productId = 101L;
        Long businessEntityId = 201L;
        
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(productId, businessEntityId))
                .thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/inventory/productId/{productId}/businessEntityId/{businessEntityId}",
                        productId, businessEntityId))
                .andExpect(status().isBadRequest());
        
        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(productId, businessEntityId);
        verifyNoMoreInteractions(mockInventoryService);
    }
}