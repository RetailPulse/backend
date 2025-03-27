package com.retailpulse.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Data
@Entity
public class SalesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long businessEntityId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_tax_id", referencedColumnName = "id")
    private SalesTax salesTax;

    private double subtotal; // subtotal of items without tax

    private double total; // total of items with tax

    @Column(nullable = false)
    @CreationTimestamp
    // Automatically set when the entity is persisted
    private Instant transactionDate;

    @PrePersist
    @PreUpdate
    private void calculateSalesTaxAndTotal() {
        double tax = subtotal * SalesTax.getTaxRate();
        double salesTaxValue = Math.round(tax * 100.0) / 100.0;
        if (salesTax != null) {
            // Update the existing salesTax value rather than creating a new instance
            salesTax.setValue(salesTaxValue);
        } else {
            // Create a new SalesTax only if it doesn't already exist
            salesTax = new SalesTax();
            salesTax.setValue(salesTaxValue);
        }
        total = subtotal + salesTax.getValue();
    }

    public static double calculateSalesTaxBySalesDetails(List<SalesDetails> salesDetails) {
        double subtotal = salesDetails.stream()
                                       .mapToDouble(detail -> detail.getSalesPricePerUnit() * detail.getQuantity())
                                       .sum();
        double tax = subtotal * SalesTax.getTaxRate();
        return Math.round(tax * 100.0) / 100.0;
    }
}
