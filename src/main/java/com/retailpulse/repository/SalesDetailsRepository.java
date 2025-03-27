package com.retailpulse.repository;

import com.retailpulse.entity.SalesDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesDetailsRepository extends JpaRepository<SalesDetails, Long> {
    List<SalesDetails> findBySaleId(Long saleId);
    void deleteBySaleId(Long saleId);
}