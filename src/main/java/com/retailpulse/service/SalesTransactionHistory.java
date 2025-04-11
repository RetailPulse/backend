package com.retailpulse.service;

import com.retailpulse.entity.SalesTransactionMemento;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SalesTransactionHistory {

    private final Map<Long, List<Map<Long, SalesTransactionMemento>>> suspendedTransactions = new ConcurrentHashMap<>();

    public List<Map<Long, SalesTransactionMemento>> suspendTransaction(Long businessEntityId, SalesTransactionMemento salesTransactionMemento) {
        if (!suspendedTransactions.containsKey(businessEntityId)) {
            List<Map<Long, SalesTransactionMemento>> suspendedTransactionsList = new ArrayList<>();
            Map<Long, SalesTransactionMemento> suspendedTransaction = new HashMap<>();
            suspendedTransaction.put(salesTransactionMemento.transactionId(), salesTransactionMemento);
            suspendedTransactionsList.add(suspendedTransaction);
            suspendedTransactions.put(businessEntityId, suspendedTransactionsList);
        } else {
            Map<Long, SalesTransactionMemento> suspendedTransaction = new HashMap<>();
            suspendedTransaction.put(salesTransactionMemento.transactionId(), salesTransactionMemento);
            suspendedTransactions.get(businessEntityId).add(suspendedTransaction);
        }

        return suspendedTransactions.get(businessEntityId);
    }

    public List<Map<Long, SalesTransactionMemento>> deleteSuspendedTransaction(Long businessEntityId, Long transactionId) {
        if (suspendedTransactions.containsKey(businessEntityId)) {
            List<Map<Long, SalesTransactionMemento>> suspendedTransactionsList = suspendedTransactions.get(businessEntityId);
            suspendedTransactionsList.removeIf(transaction -> transaction.containsKey(transactionId));
        }

        return suspendedTransactions.get(businessEntityId);
    }

}
