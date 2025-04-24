package com.retailpulse.service;

import com.retailpulse.entity.SKUCounter;
import com.retailpulse.repository.SKUCounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SKUGeneratorServiceTest {

    @Mock
    private SKUCounterRepository skuCounterRepository;

    @InjectMocks
    private SKUGeneratorService skuGeneratorService;

    @BeforeEach
    public void setUp() {
        // Open mocks if needed - using @ExtendWith handles this
    }

    @Test
    public void testGenerateSKUWhenCounterNotExists() {
        // Stub repository to simulate counter not existing
        when(skuCounterRepository.findByName("product")).thenReturn(Optional.empty());
        when(skuCounterRepository.save(any(SKUCounter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skuCounterRepository.getLastInsertedId()).thenReturn(1L);

        String generatedSKU = skuGeneratorService.generateSKU();

        // When no counter exists, a new counter is created with value 0, incremented to 1, so SKU = "RP1"
        assertEquals("RP1", generatedSKU);

        // Verify interactions
        verify(skuCounterRepository, times(1)).findByName("product");
        verify(skuCounterRepository, times(1)).save(any(SKUCounter.class));
        verify(skuCounterRepository, times(1)).getLastInsertedId();
    }
}