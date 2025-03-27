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
    void testDeleteBusinessEntity() throws Exception {
        mockMvc.perform(delete("/api/businessEntity/1"))
                .andExpect(status().isOk());

        verify(businessEntityService, times(1)).deleteBusinessEntity(1L);
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
    void testCreateBusinessEntity_InvalidJson() throws Exception {
        // Sending malformed JSON should result in a Bad Request.
        mockMvc.perform(post("/api/businessEntity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
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

    @Test
    void testDeleteBusinessEntity_Exception() throws Exception {
        // Simulate an exception thrown by the service layer during deletion.
        doThrow(new BusinessException("BUSINESS_ENTITY_NOT_FOUND", "Business Entity not found with id: 1"))
                .when(businessEntityService).deleteBusinessEntity(1L);

        mockMvc.perform(delete("/api/businessEntity/1"))
                .andExpect(status().isBadRequest());

        verify(businessEntityService, times(1)).deleteBusinessEntity(1L);
    }
}
