package com.retailpulse.repository;

import com.retailpulse.entity.SKUCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SKUCounterRepository extends JpaRepository<SKUCounter, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SKUCounter> findByName(String name);
}
