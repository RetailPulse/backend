package com.retailpulse.service;

import com.retailpulse.controller.request.BusinessEntityRequestDto;
import com.retailpulse.controller.response.BusinessEntityResponseDto;
import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.repository.BusinessEntityRepository;
import com.retailpulse.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BusinessEntityServiceTest {

    @Mock
    private BusinessEntityRepository businessEntityRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private BusinessEntityService businessEntityService;

    @Mock
    private InventoryService inventoryService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBusinessEntity_Success() {
        BusinessEntity businessEntity1 = new BusinessEntity();
        businessEntity1.setId(1L);
        businessEntity1.setName("Shop 1");
        businessEntity1.setLocation("Location 1");
        businessEntity1.setType("Shop");
        businessEntity1.setExternal(false);

        BusinessEntity businessEntity2 = new BusinessEntity();
        businessEntity2.setId(2L);
        businessEntity2.setName("Warehouse");
        businessEntity2.setLocation("Location 2");
        businessEntity2.setType("Warehouse");
        businessEntity2.setExternal(true);

        List<BusinessEntity> businessEntities = Arrays.asList(businessEntity1, businessEntity2);
        when(businessEntityRepository.findAll()).thenReturn(businessEntities);

        List<BusinessEntityResponseDto> result = businessEntityService.getAllBusinessEntities();
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        verify(businessEntityRepository, times(1)).findAll();
    }

    @Test
    void testGetBusinessEntityById_Success() {
        BusinessEntity businessEntity = new BusinessEntity();
        businessEntity.setId(1L);
        when(businessEntityRepository.findById(1L)).thenReturn(Optional.of(businessEntity));

        BusinessEntityResponseDto result = businessEntityService.getBusinessEntityById(1L);
        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void testSaveBusinessEntity_Success() {
        BusinessEntityRequestDto businessEntityRequestDto = new BusinessEntityRequestDto("Shop 1", "Location 1", "Shop", false);
        BusinessEntity businessEntity = new BusinessEntity(businessEntityRequestDto.name(), businessEntityRequestDto.location(),
                businessEntityRequestDto.type(), businessEntityRequestDto.external());

        when(businessEntityRepository.save(any(BusinessEntity.class))).thenAnswer(invocation -> {
            BusinessEntity savedBusinessEntity = invocation.getArgument(0);
            savedBusinessEntity.setId(1L);
            return savedBusinessEntity;
        });

        BusinessEntityResponseDto result = businessEntityService.saveBusinessEntity(businessEntityRequestDto);
        assertEquals(1L, result.id());
        verify(businessEntityRepository, times(1)).save(any(BusinessEntity.class));
    }

    @Test
    public void testUpdateBusinessEntity_Success() {
        Long businessId = 1L;
        BusinessEntity existingEntity = new BusinessEntity();
        existingEntity.setId(businessId);
        existingEntity.setName("Old Name");
        existingEntity.setLocation("Old Location");
        existingEntity.setType("Old Type");
        existingEntity.setExternal(false);
        existingEntity.setActive(true);

        BusinessEntityRequestDto updatedEntityDetails = new BusinessEntityRequestDto("New Name", "New Location", "New Type", true);

        when(businessEntityRepository.findById(businessId)).thenReturn(Optional.of(existingEntity));
        when(businessEntityRepository.save(any(BusinessEntity.class))).thenReturn(existingEntity);

        BusinessEntityResponseDto result = businessEntityService.updateBusinessEntity(businessId, updatedEntityDetails);

        assertEquals("New Name", result.name());
        assertEquals("New Location", result.location());
        assertEquals("New Type", result.type());
        assertTrue(result.external());
        verify(businessEntityRepository, times(1)).findById(businessId);
        verify(businessEntityRepository, times(1)).save(existingEntity);
    }

    @Test
    public void testUpdateBusinessEntity_NotFound() {
        Long businessId = 1L;
        BusinessEntityRequestDto updatedEntityDetails = new BusinessEntityRequestDto("New Name", "New Location", "New Type", false);

        when(businessEntityRepository.findById(businessId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            businessEntityService.updateBusinessEntity(businessId, updatedEntityDetails)
        );

        assertEquals("Business Entity not found with id: " + businessId, exception.getMessage());
        verify(businessEntityRepository, times(1)).findById(businessId);
        verify(businessEntityRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    public void testUpdateBusinessEntity_DeletedEntity() {
        Long businessId = 1L;
        BusinessEntity existingEntity = new BusinessEntity();
        existingEntity.setId(businessId);
        existingEntity.setName("Old Name");
        existingEntity.setLocation("Old Location");
        existingEntity.setType("Old Type");
        existingEntity.setExternal(false);
        existingEntity.setActive(false);

        BusinessEntityRequestDto updatedEntityDetails = new BusinessEntityRequestDto("New Name", "New Location", "New Type", false);

        when(businessEntityRepository.findById(businessId)).thenReturn(Optional.of(existingEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            businessEntityService.updateBusinessEntity(businessId, updatedEntityDetails)
        );

        assertEquals("Cannot update a deleted business entity with id: " + businessId, exception.getMessage());
        verify(businessEntityRepository, times(1)).findById(businessId);
        verify(businessEntityRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    public void testUpdateBusinessEntity_BusinessEntityNotFound() {
        Long businessEntityId = 1L;
        BusinessEntityRequestDto updatedBusinessEntityDetails = new BusinessEntityRequestDto("New Name", "New Location", "New Type", false);

        when(businessEntityRepository.findById(businessEntityId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            businessEntityService.updateBusinessEntity(businessEntityId, updatedBusinessEntityDetails)
        );

        assertEquals("Business Entity not found with id: " + businessEntityId, exception.getMessage());
        verify(businessEntityRepository, times(1)).findById(businessEntityId);
        verify(businessEntityRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    void testUpdateBusinessEntityDoesNotChangeIsActive_Success() {
        BusinessEntity existingBusinessEntity = new BusinessEntity();
        existingBusinessEntity.setId(1L);
        existingBusinessEntity.setName("Old Name");
        existingBusinessEntity.setLocation("Old Location");
        existingBusinessEntity.setType("Old Type");
        existingBusinessEntity.setExternal(false);
        existingBusinessEntity.setActive(true);

        BusinessEntityRequestDto updatedBusinessEntity = new BusinessEntityRequestDto("New Name", "New Location", "New Type", true);

        when(businessEntityRepository.findById(1L)).thenReturn(Optional.of(existingBusinessEntity));
        when(businessEntityRepository.save(any(BusinessEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessEntityResponseDto result = businessEntityService.updateBusinessEntity(1L, updatedBusinessEntity);

        // Assert: the fields are updated except isActive, which remains true
        assertEquals("New Name", result.name());
        assertEquals("New Location", result.location());
        assertEquals("New Type", result.type());
        assertTrue(result.external());
        assertTrue(result.active(), "BusinessEntity should remain active");
    }

    @Test
    public void testDeleteBusinessEntity_Success() {
        Long businessId = 1L;
        BusinessEntity existingEntity = new BusinessEntity();
        existingEntity.setId(businessId);
        existingEntity.setName("Old Name");
        existingEntity.setLocation("Old Location");
        existingEntity.setType("Old Type");
        existingEntity.setExternal(false);
        existingEntity.setActive(true);

        when(businessEntityRepository.findById(businessId)).thenReturn(Optional.of(existingEntity));
        when(businessEntityRepository.save(any(BusinessEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inventoryService.getInventoryByBusinessEntityId(businessId))
                .thenReturn(Collections.emptyList());

        BusinessEntityResponseDto deletedEntity = businessEntityService.deleteBusinessEntity(businessId);

        assertFalse(deletedEntity.active());
        verify(businessEntityRepository, times(1)).findById(businessId);
        verify(businessEntityRepository, times(1)).save(existingEntity);
    }

}
