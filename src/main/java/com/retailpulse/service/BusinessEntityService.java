package com.retailpulse.service;

import com.retailpulse.controller.request.BusinessEntityRequestDto;
import com.retailpulse.controller.response.BusinessEntityResponseDto;
import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.entity.Inventory;
import com.retailpulse.repository.BusinessEntityRepository;
import com.retailpulse.repository.InventoryRepository;
import com.retailpulse.service.exception.BusinessException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class BusinessEntityService {
    private static final String BUSINESS_ENTITY_NOT_FOUND = "BUSINESS_ENTITY_NOT_FOUND";
    private static final String BUSINESS_ENTITY_DELETED = "BUSINESS_ENTITY_DELETED";
    private static final String HAS_PRODUCT_IN_INVENTORY = "HAS_PRODUCT_IN_INVENTORY";

    @Autowired
    BusinessEntityRepository businessEntityRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    public List<BusinessEntityResponseDto> getAllBusinessEntities() {
        List<BusinessEntityResponseDto> businessEntities = businessEntityRepository.findAll().stream()
                .map(businessEntity -> new BusinessEntityResponseDto(
                        businessEntity.getId(),
                        businessEntity.getName(),
                        businessEntity.getLocation(),
                        businessEntity.getType(),
                        businessEntity.isExternal(),
                        businessEntity.isActive()
                ))
                .toList();

        return businessEntities;
    }

    public BusinessEntityResponseDto getBusinessEntityById(Long id) {
        BusinessEntity businessEntity = businessEntityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ENTITY_NOT_FOUND, "Business Entity not found with id: " + id));

        BusinessEntityResponseDto businessEntityResponseDto = new BusinessEntityResponseDto(businessEntity.getId(),
                businessEntity.getName(),
                businessEntity.getLocation(),
                businessEntity.getType(),
                businessEntity.isExternal(),
                businessEntity.isActive());

        return businessEntityResponseDto;
    }

    public BusinessEntityResponseDto saveBusinessEntity(BusinessEntityRequestDto request) {
        BusinessEntity businessEntity = new BusinessEntity(request.name(), request.location(), request.type(), request.external());
        BusinessEntity savedBusinessEntity = businessEntityRepository.save(businessEntity);
        return new BusinessEntityResponseDto(
                savedBusinessEntity.getId(),
                savedBusinessEntity.getName(),
                savedBusinessEntity.getLocation(),
                savedBusinessEntity.getType(),
                savedBusinessEntity.isExternal(),
                savedBusinessEntity.isActive()
        );
    }

    public BusinessEntityResponseDto updateBusinessEntity(Long id, BusinessEntityRequestDto businessEntityDetails) {
        BusinessEntity businessEntity = businessEntityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ENTITY_NOT_FOUND, "Business Entity not found with id: " + id));

        if (!businessEntity.isActive()) {
            throw new BusinessException(BUSINESS_ENTITY_DELETED, "Cannot update a deleted business entity with id: " + id);
        }

        // Update fields from the incoming details if provided
        updateField(businessEntityDetails.name(), businessEntity::setName);
        updateField(businessEntityDetails.location(), businessEntity::setLocation);
        updateField(businessEntityDetails.type(), businessEntity::setType);
        updateField(businessEntityDetails.external(), businessEntity::setExternal);

        BusinessEntity updatedBusinessEntity = businessEntityRepository.save(businessEntity);

        return new BusinessEntityResponseDto(
                updatedBusinessEntity.getId(),
                updatedBusinessEntity.getName(),
                updatedBusinessEntity.getLocation(),
                updatedBusinessEntity.getType(),
                updatedBusinessEntity.isExternal(),
                updatedBusinessEntity.isActive());
    }

    // Generic helper method for updating fields
    private <T> void updateField(T newValue, Consumer<T> updater) {
        if(newValue == null) {
            return;
        }
        if (newValue instanceof String && ((String) newValue).isEmpty()) {
            return;
        }
        updater.accept(newValue);
    }

    public BusinessEntityResponseDto deleteBusinessEntity(Long id) {
        BusinessEntity businessEntity = businessEntityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ENTITY_NOT_FOUND, "Business Entity not found with id: " + id));

        if (!businessEntity.isActive()) {
            throw new BusinessException(BUSINESS_ENTITY_DELETED, "Business Entity with id " + id + " is already deleted.");
        }

        // Check if Inventory has products; If yes, cannot delete
        if (hasProductsInInventory(businessEntity.getId())) {
            throw new BusinessException(HAS_PRODUCT_IN_INVENTORY, "Cannot delete Business Entity with id " + id + " as it has associated products in the inventory.");
        }

        businessEntity.setActive(false);

        BusinessEntity updatedBusinessEntity = businessEntityRepository.save(businessEntity);

        return new BusinessEntityResponseDto(
                updatedBusinessEntity.getId(),
                updatedBusinessEntity.getName(),
                updatedBusinessEntity.getLocation(),
                updatedBusinessEntity.getType(),
                updatedBusinessEntity.isExternal(),
                updatedBusinessEntity.isActive());
    }

    private boolean hasProductsInInventory(@NotNull Long id) {
        List<Inventory> inventories = inventoryRepository.findByBusinessEntityId(id);
        return inventories.stream().anyMatch(inventory -> inventory.getQuantity() > 0);
    }

}
