package com.retailpulse.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class SalesTax {

    public static final double TAX_RATE = 0.09;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private double value;

    public static double getTaxRate() {
        return TAX_RATE;
    }
}
