package com.retailpulse.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class SalesDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long saleId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double salesPricePerUnit;
}
