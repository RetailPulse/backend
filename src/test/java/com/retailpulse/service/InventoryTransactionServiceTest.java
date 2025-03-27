package com.retailpulse.service;

import com.retailpulse.DTO.InventoryTransactionProductDto;
import com.retailpulse.entity.BusinessEntity;
import com.retailpulse.entity.Inventory;
import com.retailpulse.entity.InventoryTransaction;
import com.retailpulse.entity.Product;
import com.retailpulse.repository.BusinessEntityRepository;
import com.retailpulse.repository.InventoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryTransactionServiceTest {

    @Mock
    private InventoryTransactionRepository mockInventoryTransactionRepository;

    @Mock
    private InventoryService mockInventoryService;

    @Mock
    private ProductService mockProductService;

    @Mock
    private BusinessEntityRepository mockBusinessEntityRepository;

    @InjectMocks
    private InventoryTransactionService inventoryTransactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllInventoryTransactionWithProduct() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setDescription("Product A");

        InventoryTransaction inventoryTransaction = new InventoryTransaction();
        inventoryTransaction.setProductId(1L);

        InventoryTransactionProductDto dto1 = new InventoryTransactionProductDto(inventoryTransaction, product);

        List<InventoryTransactionProductDto> mockDtos = Collections.singletonList(dto1);

        when(mockInventoryTransactionRepository.findAllWithProduct()).thenReturn(mockDtos);

        // Act
        List<InventoryTransactionProductDto> result = inventoryTransactionService.getAllInventoryTransactionWithProduct();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Product A", result.get(0).getProduct().getDescription());

        verify(mockInventoryTransactionRepository, times(1)).findAllWithProduct();
        verifyNoMoreInteractions(mockInventoryTransactionRepository);
    }

    @Test
    void testSaveInventoryTransaction_Successful() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        Inventory sourceInventory = new Inventory();
        sourceInventory.setId(1L);
        sourceInventory.setProductId(1L);
        sourceInventory.setBusinessEntityId(101L);
        sourceInventory.setQuantity(20);
        sourceInventory.setTotalCostPrice(100.0);

        Inventory updatedSourceInventory = new Inventory();
        updatedSourceInventory.setId(1L);
        updatedSourceInventory.setProductId(1L);
        updatedSourceInventory.setBusinessEntityId(101L);
        updatedSourceInventory.setQuantity(10);
        updatedSourceInventory.setTotalCostPrice(50.0);

        Inventory destinationInventory = new Inventory();
        destinationInventory.setId(2L);
        destinationInventory.setProductId(1L);
        destinationInventory.setBusinessEntityId(201L);
        destinationInventory.setQuantity(30);
        destinationInventory.setTotalCostPrice(150.0);

        Inventory updatedDestinationInventory = new Inventory();
        updatedDestinationInventory.setId(2L);
        updatedDestinationInventory.setProductId(1L);
        updatedDestinationInventory.setBusinessEntityId(201L);
        updatedDestinationInventory.setQuantity(40);
        updatedDestinationInventory.setTotalCostPrice(200.0);

        BusinessEntity businessEntity = new BusinessEntity("name", "location", "type", false);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));
        when(mockBusinessEntityRepository.findById(101L)).thenReturn(Optional.of(businessEntity));
        when(mockBusinessEntityRepository.findById(201L)).thenReturn(Optional.of(businessEntity));
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(1L, 101L)).thenReturn(Optional.of(sourceInventory));
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(1L, 201L)).thenReturn(Optional.of(destinationInventory));
        when(mockInventoryService.updateInventory(1L, updatedSourceInventory)).thenReturn(updatedSourceInventory);
        when(mockInventoryService.updateInventory(2L, updatedDestinationInventory)).thenReturn(updatedDestinationInventory);
        when(mockInventoryTransactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        InventoryTransaction result = inventoryTransactionService.saveInventoryTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(101L, result.getSource());
        assertEquals(201L, result.getDestination());
        assertEquals(10, result.getQuantity());
        assertEquals(5.0, result.getCostPricePerUnit(), 0.01);

        verify(mockProductService, times(1)).getProductById(1L);
        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(1L, 101L);
        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(1L, 201L);
        verify(mockInventoryService, times(1)).updateInventory(1L, updatedSourceInventory);
        verify(mockInventoryService, times(1)).updateInventory(2L, updatedDestinationInventory);
        verify(mockInventoryTransactionRepository, times(1)).save(transaction);
        verifyNoMoreInteractions(mockProductService, mockInventoryService, mockInventoryTransactionRepository);
    }

    @Test
    void testSaveInventoryTransaction_InsufficientSourceQuantity() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(30);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        Inventory sourceInventory = new Inventory();
        sourceInventory.setId(1L);
        sourceInventory.setProductId(1L);
        sourceInventory.setBusinessEntityId(101L);
        sourceInventory.setQuantity(20);
        sourceInventory.setTotalCostPrice(100.0);

        BusinessEntity businessEntity = new BusinessEntity("name", "location", "type", false);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));
        when(mockBusinessEntityRepository.findById(101L)).thenReturn(Optional.of(businessEntity));
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(1L, 101L)).thenReturn(Optional.of(sourceInventory));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryTransactionService.saveInventoryTransaction(transaction);
        });

        assertEquals(
                "Not enough quantity in source inventory for product id: 1 and source id: 101. Available: 20, required: 30",
                exception.getMessage()
        );

        verify(mockProductService, times(1)).getProductById(1L);
        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(1L, 101L);
        verifyNoMoreInteractions(mockProductService, mockInventoryService);
    }

    @Test
    void testSaveInventoryTransaction_InvalidProduct() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(999L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);

        when(mockProductService.getProductById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryTransactionService.saveInventoryTransaction(transaction);
        });

        assertEquals("Product not found for product id: 999", exception.getMessage());

        verify(mockProductService, times(1)).getProductById(999L);
        verifyNoMoreInteractions(mockProductService);
    }

    @Test
    void testSaveInventoryTransaction_SourceSameAsDestination() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(101L);
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryTransactionService.saveInventoryTransaction(transaction);
        });

        assertEquals("Source and Destination cannot be the same", exception.getMessage());

        verify(mockProductService, times(1)).getProductById(1L);
        verifyNoMoreInteractions(mockProductService);
    }

    @Test
    void testSaveInventoryTransaction_NegativeQuantity() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(-5);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryTransactionService.saveInventoryTransaction(transaction);
        });

        assertEquals("Quantity cannot be negative or zero", exception.getMessage());

        verify(mockProductService, times(1)).getProductById(1L);
        verifyNoMoreInteractions(mockProductService);
    }

    @Test
    void testSaveInventoryTransaction_SourceExternal() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        Inventory destinationInventory = new Inventory();
        destinationInventory.setId(2L);
        destinationInventory.setProductId(1L);
        destinationInventory.setBusinessEntityId(201L);
        destinationInventory.setQuantity(30);
        destinationInventory.setTotalCostPrice(150.0);

        Inventory updatedDestinationInventory = new Inventory();
        updatedDestinationInventory.setId(2L);
        updatedDestinationInventory.setProductId(1L);
        updatedDestinationInventory.setBusinessEntityId(201L);
        updatedDestinationInventory.setQuantity(destinationInventory.getQuantity() + 10);
        updatedDestinationInventory.setTotalCostPrice(destinationInventory.getTotalCostPrice() + (5.0 * 10));

        BusinessEntity businessEntityFalse = new BusinessEntity("name", "location", "type", false);
        BusinessEntity businessEntityTrue = new BusinessEntity("name", "location", "type", true);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));
        // Source is external: skip source inventory validation/update.
        when(mockBusinessEntityRepository.findById(101L)).thenReturn(Optional.of(businessEntityTrue));
        // Destination is not external.
        when(mockBusinessEntityRepository.findById(201L)).thenReturn(Optional.of(businessEntityFalse));
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(1L, 201L))
                .thenReturn(Optional.of(destinationInventory));
        when(mockInventoryService.updateInventory(2L, updatedDestinationInventory)).thenReturn(updatedDestinationInventory);
        when(mockInventoryTransactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        InventoryTransaction result = inventoryTransactionService.saveInventoryTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(101L, result.getSource());
        assertEquals(201L, result.getDestination());
        assertEquals(10, result.getQuantity());
        assertEquals(5.0, result.getCostPricePerUnit(), 0.01);

        verify(mockProductService, times(1)).getProductById(1L);
        // Destination inventory update
        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(1L, 201L);
        verify(mockInventoryService, times(1)).updateInventory(2L, updatedDestinationInventory);
        // Source inventory should not be fetched/updated since it is external
        verify(mockInventoryTransactionRepository, times(1)).save(transaction);
    }

    @Test
    void testSaveInventoryTransaction_DestinationExternal() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        Inventory sourceInventory = new Inventory();
        sourceInventory.setId(1L);
        sourceInventory.setProductId(1L);
        sourceInventory.setBusinessEntityId(101L);
        sourceInventory.setQuantity(20);
        sourceInventory.setTotalCostPrice(100.0);

        Inventory updatedSourceInventory = new Inventory();
        updatedSourceInventory.setId(1L);
        updatedSourceInventory.setProductId(1L);
        updatedSourceInventory.setBusinessEntityId(101L);
        updatedSourceInventory.setQuantity(sourceInventory.getQuantity() - 10);
        updatedSourceInventory.setTotalCostPrice(sourceInventory.getTotalCostPrice() - (5.0 * 10));

        BusinessEntity businessEntityFalse = new BusinessEntity("name", "location", "type", false);
        BusinessEntity businessEntityTrue = new BusinessEntity("name", "location", "type", true);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));
        // Source is not external.
        when(mockBusinessEntityRepository.findById(101L)).thenReturn(Optional.of(businessEntityFalse));
        // Destination is external: skip destination inventory update.
        when(mockBusinessEntityRepository.findById(201L)).thenReturn(Optional.of(businessEntityTrue));
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(1L, 101L)).thenReturn(Optional.of(sourceInventory));
        when(mockInventoryService.updateInventory(1L, updatedSourceInventory)).thenReturn(updatedSourceInventory);
        when(mockInventoryTransactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        InventoryTransaction result = inventoryTransactionService.saveInventoryTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(101L, result.getSource());
        assertEquals(201L, result.getDestination());
        assertEquals(10, result.getQuantity());
        assertEquals(5.0, result.getCostPricePerUnit(), 0.01);

        verify(mockProductService, times(1)).getProductById(1L);
        verify(mockInventoryService, times(1)).getInventoryByProductIdAndBusinessEntityId(1L, 101L);
        verify(mockInventoryService, times(1)).updateInventory(1L, updatedSourceInventory);
        // Destination inventory calls should not be made.
        verify(mockInventoryTransactionRepository, times(1)).save(transaction);
    }

    @Test
    void testSaveInventoryTransaction_BothExternal() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        BusinessEntity businessEntityTrue = new BusinessEntity("name", "location", "type", true);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));
        // Both source and destination are external; skip inventory modifications.
        when(mockBusinessEntityRepository.findById(101L)).thenReturn(Optional.of(businessEntityTrue));
        when(mockBusinessEntityRepository.findById(201L)).thenReturn(Optional.of(businessEntityTrue));
        when(mockInventoryTransactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        InventoryTransaction result = inventoryTransactionService.saveInventoryTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(101L, result.getSource());
        assertEquals(201L, result.getDestination());
        assertEquals(10, result.getQuantity());
        assertEquals(5.0, result.getCostPricePerUnit(), 0.01);

        verify(mockProductService, times(1)).getProductById(1L);
        // No inventory service calls for inventory updates expected.
        verify(mockInventoryTransactionRepository, times(1)).save(transaction);
    }

    @Test
    void testSaveInventoryTransaction_NewDestinationInventory() {
        // Arrange
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(1L);
        transaction.setSource(101L);
        transaction.setDestination(201L);
        transaction.setQuantity(10);
        transaction.setCostPricePerUnit(5.0);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        Inventory sourceInventory = new Inventory();
        sourceInventory.setId(1L);
        sourceInventory.setProductId(1L);
        sourceInventory.setBusinessEntityId(101L);
        sourceInventory.setQuantity(20);
        sourceInventory.setTotalCostPrice(100.0);

        Inventory updatedSourceInventory = new Inventory();
        updatedSourceInventory.setId(1L);
        updatedSourceInventory.setProductId(1L);
        updatedSourceInventory.setBusinessEntityId(101L);
        updatedSourceInventory.setQuantity(sourceInventory.getQuantity() - 10);
        updatedSourceInventory.setTotalCostPrice(sourceInventory.getTotalCostPrice() - (5.0 * 10));

        BusinessEntity businessEntityFalse = new BusinessEntity("name", "location", "type", false);

        when(mockProductService.getProductById(1L)).thenReturn(Optional.of(product));
        when(mockBusinessEntityRepository.findById(101L)).thenReturn(Optional.of(businessEntityFalse));
        when(mockBusinessEntityRepository.findById(201L)).thenReturn(Optional.of(businessEntityFalse));
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(1L, 101L))
                .thenReturn(Optional.of(sourceInventory));
        // Destination inventory does not exist.
        when(mockInventoryService.getInventoryByProductIdAndBusinessEntityId(1L, 201L))
                .thenReturn(Optional.empty());
        // Stub saveInventory to return the passed inventory
        when(mockInventoryService.saveInventory(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mockInventoryService.updateInventory(1L, updatedSourceInventory))
                .thenReturn(updatedSourceInventory);
        when(mockInventoryTransactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        InventoryTransaction result = inventoryTransactionService.saveInventoryTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(101L, result.getSource());
        assertEquals(201L, result.getDestination());
        assertEquals(10, result.getQuantity());
        assertEquals(5.0, result.getCostPricePerUnit(), 0.01);

        verify(mockProductService, times(1)).getProductById(1L);
        verify(mockInventoryService, times(1))
                .getInventoryByProductIdAndBusinessEntityId(1L, 101L);
        verify(mockInventoryService, times(1))
                .updateInventory(1L, updatedSourceInventory);
        verify(mockInventoryService, times(1))
                .getInventoryByProductIdAndBusinessEntityId(1L, 201L);
        verify(mockInventoryService, times(1))
                .saveInventory(argThat(inv ->
                        inv.getProductId() == 1L &&
                        inv.getBusinessEntityId() == 201L &&
                        inv.getQuantity() == 10 &&
                        inv.getTotalCostPrice() == 5.0 * 10));
        verify(mockInventoryTransactionRepository, times(1)).save(transaction);
    }
}