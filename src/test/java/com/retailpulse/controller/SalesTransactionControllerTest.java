package com.retailpulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailpulse.DTO.SalesTransactionDetailsDto;
import com.retailpulse.entity.SalesTransaction;
import com.retailpulse.service.SalesTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SalesTransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SalesTransactionService salesTransactionService;

    @InjectMocks
    private SalesTransactionController salesTransactionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(salesTransactionController).build();
    }

    @Test
    public void testCalculateSalesTax() throws Exception {
        // Given
        double expectedTax = 15.75;
        when(salesTransactionService.calculateSalesTax(ArgumentMatchers.anyList())).thenReturn(expectedTax);

        // When & Then
        mockMvc.perform(post("/api/sales/calculateSalesTax")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedTax)));
    }

    @Test
    public void testGetSalesTransactionFound() throws Exception {
        // Given
        SalesTransaction transaction = new SalesTransaction();
        when(salesTransactionService.getSalesTransaction(1L)).thenReturn(Optional.of(transaction));

        // When & Then
        mockMvc.perform(get("/api/sales/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetSalesTransactionNotFound() throws Exception {
        // Given
        when(salesTransactionService.getSalesTransaction(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/sales/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetFullTransactionNotFound() throws Exception {
        // Given
        when(salesTransactionService.getFullTransaction(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/sales/1/full"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateSalesTransaction() throws Exception {
        // Given
        SalesTransaction transaction = new SalesTransaction();
        when(salesTransactionService.createSalesTransaction(ArgumentMatchers.eq(10L), ArgumentMatchers.anyList()))
                .thenReturn(transaction);

        // When & Then
        mockMvc.perform(post("/api/sales/createTransaction/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSalesTransaction() throws Exception {
        // Given
        SalesTransaction updatedTransaction = new SalesTransaction();
        when(salesTransactionService.updateSalesTransaction(ArgumentMatchers.eq(5L), ArgumentMatchers.anyList()))
                .thenReturn(updatedTransaction);

        // When & Then
        mockMvc.perform(put("/api/sales/updateSalesTransaction/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk());
    }
}
