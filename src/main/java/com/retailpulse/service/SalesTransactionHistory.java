package com.retailpulse.service;

import com.retailpulse.entity.SalesTransactionMemento;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SalesTransactionHistory {

    private final Map<Long, List<SalesTransactionMemento>> suspendedTransactions =  new ConcurrentHashMap<>();

    public void suspendTransaction(Long businessEntityId, SalesTransactionMemento salesTransactionMemento) {
        if (!suspendedTransactions.containsKey(businessEntityId)) {
            List<SalesTransactionMemento> suspendedTransactionsList = new ArrayList<>();
            suspendedTransactionsList.add(salesTransactionMemento);
            suspendedTransactions.put(businessEntityId, suspendedTransactionsList);
        } else {
            suspendedTransactions.get(businessEntityId).add(salesTransactionMemento);
        }
    }

    public List<SalesTransactionMemento> getSuspendedTransactions(Long businessEntityId) {
        return suspendedTransactions.get(businessEntityId);
    }

}
