package com.retailpulse.controller;

import com.retailpulse.controller.request.BusinessEntityRequestDto;
import com.retailpulse.controller.response.BusinessEntityResponseDto;
import com.retailpulse.exception.GlobalExceptionHandler;
import com.retailpulse.service.BusinessEntityService;
import com.retailpulse.service.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BusinessEntityControllerTest {

    /*
     * Test suite for BusinessEntityController.
     * <p>
     * Covered Scenarios:
     *
     * GET /api/businessEntity:
     * - Successfully retrieves all business entities.
     * <p>
     * GET /api/businessEntity/{id}:
     * - Successfully retrieves a business entity by ID.
     * - Returns error when the entity is not found.
     * <p>
     * POST /api/businessEntity:
     * - Successfully creates a business entity.
     * - Returns error for invalid/malformed requests or missing required fields.
     * <p>
     * PUT /api/businessEntity/{id}:
     * - Successfully updates an existing business entity.
     * - Returns error for invalid/malformed requests or when the entity is not found.
     * <p>
     * DELETE /api/businessEntity/{id}:
     * - Successfully deletes a business entity.
     * - Returns error when trying to delete a non-existent entity.
     */

    @Mock
    private BusinessEntityService businessEntityService;

    @InjectMocks
    private BusinessEntityController businessEntityController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(businessEntityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- GET /api/businessEntity ---

    @Test
    void testGetAllBusinessEntities() throws Exception {
        BusinessEntityResponseDto businessEntityResponseDto1 = new BusinessEntityResponseDto(
                1L,
                "BusinessEntity 1",
                "Location 1",
                "Type 1",
                false,
                true
        );

        BusinessEntityResponseDto businessEntityResponseDto2 = new BusinessEntityResponseDto(
                2L,
                "BusinessEntity 2",
                "Location 2",
                "Type 2",
                false,
                true
        );

        List<BusinessEntityResponseDto> businessEntities = Arrays.asList(businessEntityResponseDto1, businessEntityResponseDto2);
        when(businessEntityService.getAllBusinessEntities()).thenReturn(businessEntities);

        mockMvc.perform(get("/api/businessEntity"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'id': 1, 'name': 'BusinessEntity 1', 'location': 'Location 1', 'type': 'Type 1'}, {'id': 2, 'name': 'BusinessEntity 2', 'location': 'Location 2', 'type': 'Type 2'}]"));

        verify(businessEntityService, times(1)).getAllBusinessEntities();
    }

    @Test
    void testGetAllBusinessEntities_Success() throws Exception {
        BusinessEntityResponseDto entity1 = new BusinessEntityResponseDto(1L, "Entity1", "Location1", "Type1", false, true);
        BusinessEntityResponseDto entity2 = new BusinessEntityResponseDto(2L, "Entity2", "Location2", "Type2", true, true);
        List<BusinessEntityResponseDto> entities = List.of(entity1, entity2);

        when(businessEntityService.getAllBusinessEntities()).thenReturn(entities);

        mockMvc.perform(get("/api/businessEntity"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"name\":\"Entity1\",\"location\":\"Location1\",\"type\":\"Type1\",\"external\":false},{\"id\":2,\"name\":\"Entity2\",\"location\":\"Location2\",\"type\":\"Type2\",\"external\":true}]"));

        verify(businessEntityService, times(1)).getAllBusinessEntities();
    }

    // --- GET /api/businessEntity/{id} ---

    @Test
    void testGetBusinessEntityById() throws Exception {
        BusinessEntityResponseDto businessEntityResponseDto1 = new BusinessEntityResponseDto(
                1L,
                "BusinessEntity 1",
                "Location 1",
                "Type 1",
                false,
                true
        );

        when(businessEntityService.getBusinessEntityById(1L)).thenReturn(businessEntityResponseDto1);

        mockMvc.perform(get("/api/businessEntity/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'id': 1, 'name': 'BusinessEntity 1', 'location': 'Location 1', 'type': 'Type 1'}"));

        verify(businessEntityService, times(1)).getBusinessEntityById(1L);
    }

    @Test
    void testGetBusinessById_Success() throws Exception {
        BusinessEntityResponseDto entity = new BusinessEntityResponseDto(1L, "Entity1", "Location1", "Type1", false, true);

        when(businessEntityService.getBusinessEntityById(1L)).thenReturn(entity);

        mockMvc.perform(get("/api/businessEntity/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"name\":\"Entity1\",\"location\":\"Location1\",\"type\":\"Type1\",\"external\":false}"));

        verify(businessEntityService, times(1)).getBusinessEntityById(1L);
    }

    @Test
    void testGetBusinessEntityByIdNotFound() throws Exception {
        when(businessEntityService.getBusinessEntityById(1L))
                .thenThrow(new BusinessException("BUSINESS_ENTITY_NOT_FOUND", "Business Entity not found with id: 1"));

        mockMvc.perform(get("/api/businessEntity/1"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Exception resolvedException = result.getResolvedException();
                    assertNotNull(resolvedException);
                    assertInstanceOf(BusinessException.class, resolvedException);
                    BusinessException ex = (BusinessException) resolvedException;
                    assertEquals("Business Entity not found with id: 1", ex.getMessage());
                });

        verify(businessEntityService, times(1)).getBusinessEntityById(1L);
    }

    @Test
    void testGetBusinessById_NotFound() throws Exception {
        when(businessEntityService.getBusinessEntityById(1L)).thenThrow(new BusinessException("BUSINESS_ENTITY_NOT_FOUND", "Entity not found"));

        mockMvc.perform(get("/api/businessEntity/1"))
                .andExpect(status().isBadRequest());

        verify(businessEntityService, times(1)).getBusinessEntityById(1L);
    }

    // --- POST /api/businessEntity ---

    @Test
    void testCreateBusinessEntity() throws Exception {
        BusinessEntityRequestDto businessEntityRequestDto = new BusinessEntityRequestDto("BusinessEntity 1", "Location 1", "Type 1", false);
        BusinessEntityResponseDto businessEntityResponseDto = new BusinessEntityResponseDto(1L, "BusinessEntity 1", "Location 1", "Type 1", false, true);

        when(businessEntityService.saveBusinessEntity(businessEntityRequestDto)).thenReturn(businessEntityResponseDto);

        mockMvc.perform(post("/api/businessEntity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"BusinessEntity 1\", \"location\": \"Location 1\", \"type\": \"Type 1\", \"external\": false}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'id': 1, 'name': 'BusinessEntity 1', 'location': 'Location 1', 'type': 'Type 1', 'external': false}"));

        verify(businessEntityService, times(1)).saveBusinessEntity(businessEntityRequestDto);
    }

    @Test
    void testCreateBusinessEntity_Success() throws Exception {
        BusinessEntityRequestDto request = new BusinessEntityRequestDto("Entity1", "Location1", "Type1", false);
        BusinessEntityResponseDto response = new BusinessEntityResponseDto(1L, "Entity1", "Location1", "Type1", false, true);

        when(businessEntityService.saveBusinessEntity(request)).thenReturn(response);

        mockMvc.perform(post("/api/businessEntity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Entity1\",\"location\":\"Location1\",\"type\":\"Type1\",\"external\":false}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"name\":\"Entity1\",\"location\":\"Location1\",\"type\":\"Type1\",\"external\":false}"));

        verify(businessEntityService, times(1)).saveBusinessEntity(request);
    }

    @Test
    void testCreateBusinessEntity_InvalidJson() throws Exception {
        // Sending malformed JSON should result in a Bad Request.
        mockMvc.perform(post("/api/businessEntity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBusinessEntity_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/businessEntity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"Location1\",\"type\":\"Type1\",\"external\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBusinessEntity_MissingName() throws Exception {
        // Missing 'name' field in JSON
        mockMvc.perform(post("/api/businessEntity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\": \"Location 1\", \"type\": \"Type 1\", \"external\": false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBusinessEntity_MissingRequiredFields() throws Exception {
        // Missing 'location', 'type', and 'external' fields in JSON
        mockMvc.perform(post("/api/businessEntity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"BusinessEntity 1\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /api/businessEntity/{id} ---

    @Test
    void testUpdateBusinessEntity() throws Exception {
        BusinessEntityRequestDto businessEntityRequestDto = new BusinessEntityRequestDto(
                "Updated BusinessEntity",
                "Updated Location",
                "Updated Type",
                false);

        BusinessEntityResponseDto businessEntityResponseDto = new BusinessEntityResponseDto(
                1L,
                "Updated BusinessEntity",
                "Updated Location",
                "Updated Type",
                false,
                true
        );

        when(businessEntityService.updateBusinessEntity(1L, businessEntityRequestDto)).thenReturn(businessEntityResponseDto);

        mockMvc.perform(put("/api/businessEntity/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated BusinessEntity\", \"location\": \"Updated Location\", \"type\": \"Updated Type\", \"external\": false}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'id': 1, 'name': 'Updated BusinessEntity', 'location': 'Updated Location', 'type': 'Updated Type', 'external': false, 'active': true}"));

        verify(businessEntityService, times(1)).updateBusinessEntity(anyLong(), any(BusinessEntityRequestDto.class));
    }

    @Test
    void testUpdateBusinessEntity_Success() throws Exception {
        BusinessEntityRequestDto request = new BusinessEntityRequestDto("UpdatedEntity", "UpdatedLocation", "UpdatedType", true);
        BusinessEntityResponseDto response = new BusinessEntityResponseDto(1L, "UpdatedEntity", "UpdatedLocation", "UpdatedType", true, true);

        when(businessEntityService.updateBusinessEntity(1L, request)).thenReturn(response);

        mockMvc.perform(put("/api/businessEntity/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UpdatedEntity\",\"location\":\"UpdatedLocation\",\"type\":\"UpdatedType\",\"external\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"name\":\"UpdatedEntity\",\"location\":\"UpdatedLocation\",\"type\":\"UpdatedType\",\"external\":true}"));

        verify(businessEntityService, times(1)).updateBusinessEntity(1L, request);
    }

    @Test
    void testUpdateBusinessEntity_Exception() throws Exception {
        // Simulate an exception thrown by the service layer during an update.
        when(businessEntityService.updateBusinessEntity(anyLong(), any(BusinessEntityRequestDto.class)))
                .thenThrow(new BusinessException("BUSINESS_ENTITY_NOT_FOUND", "Business Entity not found with id: 1"));

        mockMvc.perform(put("/api/businessEntity/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated BusinessEntity\", \"location\": \"Updated Location\", \"type\": \"Updated Type\"}"))
                .andExpect(status().isBadRequest());

        verify(businessEntityService, times(1)).updateBusinessEntity(anyLong(), any(BusinessEntityRequestDto.class));
    }

    // --- DELETE /api/businessEntity/{id} ---

    @Test
    void testDeleteBusinessEntity() throws Exception {
        mockMvc.perform(delete("/api/businessEntity/1"))
                .andExpect(status().isOk());

        verify(businessEntityService, times(1)).deleteBusinessEntity(1L);
    }

    @Test
    void testDeleteBusinessEntity_Success() throws Exception {
        mockMvc.perform(delete("/api/businessEntity/1"))
                .andExpect(status().isOk());

        verify(businessEntityService, times(1)).deleteBusinessEntity(1L);
    }

    @Test
    void testDeleteBusinessEntity_Exception() throws Exception {
        // Simulate an exception thrown by the service layer during deletion.
        doThrow(new BusinessException("BUSINESS_ENTITY_NOT_FOUND", "Business Entity not found with id: 1"))
                .when(businessEntityService).deleteBusinessEntity(1L);

        mockMvc.perform(delete("/api/businessEntity/1"))
                .andExpect(status().isBadRequest());

        verify(businessEntityService, times(1)).deleteBusinessEntity(1L);
    }

    @Test
    void testDeleteBusinessEntity_NotFound() throws Exception {
        doThrow(new BusinessException("BUSINESS_ENTITY_NOT_FOUND", "Entity not found"))
                .when(businessEntityService).deleteBusinessEntity(1L);

        mockMvc.perform(delete("/api/businessEntity/1"))
                .andExpect(status().isBadRequest());

        verify(businessEntityService, times(1)).deleteBusinessEntity(1L);
    }
}
