package com.retailpulse.repository;

import com.retailpulse.entity.SKUCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SKUCounterRepository extends JpaRepository<SKUCounter, Long> {
    Optional<SKUCounter> findByName(String name);
}
