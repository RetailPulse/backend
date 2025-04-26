package com.retailpulse.service.dataExtractor;

public interface TableDataExtractor<T> {
    Object[] getRowData(T item, int serialNumber);
}
