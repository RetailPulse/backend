package com.retailpulse.service;

import com.retailpulse.entity.SKUCounter;
import com.retailpulse.repository.SKUCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SKUGeneratorService {

    @Autowired
    private SKUCounterRepository skuCounterRepository;

    private static  final String COUNTER_NAME = "product";

    public String generateSKU() {
        // Find or create a SKU counter
        SKUCounter skuCounter = skuCounterRepository.findByName(COUNTER_NAME)
                .orElseGet(() -> {
                    SKUCounter newCounter = new SKUCounter(COUNTER_NAME, 0L);
                    return skuCounterRepository.save(newCounter);
                });

        // Increment the counter
        skuCounter.setCounter(skuCounter.getCounter() + 1);

        // Save the updated counter
        skuCounterRepository.save(skuCounter);

        // Return the SKU in the format "RP1", "RP2", "RP3", etc.
        return "RP" + skuCounter.getCounter();
    }
}
