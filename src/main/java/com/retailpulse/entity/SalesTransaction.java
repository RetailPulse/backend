package com.retailpulse.entity;

import com.retailpulse.controller.request.SalesDetailsDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class SalesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long businessEntityId;

    @ManyToOne
    @JoinColumn(name = "sales_tax_id")
    private SalesTax salesTax;

    private BigDecimal salesTaxAmount;

    private BigDecimal subtotal; // subtotal of items without tax

    private BigDecimal total; // total of items with tax

    @Column(nullable = false)
    @CreationTimestamp
    // Automatically set when the entity is persisted
    private Instant transactionDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "salesTransaction", orphanRemoval = true)
    private List<SalesDetails> salesDetailEntities = new ArrayList<>();

    protected SalesTransaction() {}

    public SalesTransaction(Long businessEntityId, SalesTax salesTax) {
        this.businessEntityId = businessEntityId;
        this.salesTax = salesTax;
    }

    public void addSalesDetails(SalesDetails detail) {
        detail.setSalesTransaction(this);
        salesDetailEntities.add(detail);
        recalculateTotal();
    }

    public void updateSalesDetails(List<SalesDetails> details) {
        this.salesDetailEntities.clear();

        for (SalesDetails detail : details) {
            this.addSalesDetails(detail);
        }
    }

    public SalesTransactionMemento saveToMemento() {
        return new SalesTransactionMemento(
                this.businessEntityId,
                this.subtotal.toPlainString(),
                this.salesTax.getTaxType().name(),
                this.salesTax.getTaxRate().toPlainString(),
                this.salesTaxAmount.toPlainString(),
                this.total.toPlainString(),
                this.salesDetailEntities.stream().map(
                        salesDetails -> new SalesDetailsDto(
                                salesDetails.getProductId(),
                                salesDetails.getQuantity(),
                                salesDetails.getSalesPricePerUnit().toString()
                        )
                ).toList()
        );
    }

    private void recalculateTotal() {
        BigDecimal subtotal = salesDetailEntities.stream()
                .map(SalesDetails::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        this.subtotal = subtotal;
        this.salesTaxAmount = salesTax.calculateTax(subtotal);
        this.total = subtotal.add(salesTaxAmount).setScale(2, RoundingMode.HALF_UP);
    }

}
