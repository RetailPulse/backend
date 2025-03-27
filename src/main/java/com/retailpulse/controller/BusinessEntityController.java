package com.retailpulse.controller;

import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.controller.request.BusinessEntityRequestDto;
import com.retailpulse.controller.response.BusinessEntityResponseDto;
import com.retailpulse.service.BusinessEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/businessEntity")
public class BusinessEntityController {

    private static final Logger logger = Logger.getLogger(BusinessEntityController.class.getName());
    private static final String INVALID_REQUEST = "INVALID_REQUEST";

    @Autowired
    BusinessEntityService businessEntityService;

    @GetMapping
    public ResponseEntity<List<BusinessEntityResponseDto>> getAllBusinessEntities() {
        logger.info("Fetching all business entities");
        List<BusinessEntityResponseDto> entities = businessEntityService.getAllBusinessEntities();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessEntityResponseDto> getBusinessById(@PathVariable Long id) {
        logger.info("Fetching business entity with id: " + id);
        BusinessEntityResponseDto response = businessEntityService.getBusinessEntityById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BusinessEntityResponseDto> createBusinessEntity(@RequestBody BusinessEntityRequestDto request) {
        logger.info("Received request to create business entity: " + request);

        if (!StringUtils.hasText(request.name()) ||
                !StringUtils.hasText(request.location()) ||
                !StringUtils.hasText(request.type()) ||
                request.external() == null) {
            throw new ApplicationException(INVALID_REQUEST, "Name, location, type, and external are required fields");
        }

        BusinessEntityResponseDto response = businessEntityService.saveBusinessEntity(request);
        logger.info("Successfully created business entity");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessEntityResponseDto> updateBusinessEntity(@PathVariable Long id, @RequestBody BusinessEntityRequestDto request) {
        logger.info("Received request to update business entity with id: " + id);
        BusinessEntityResponseDto response = businessEntityService.updateBusinessEntity(id, request);
        logger.info("Successfully updated business entity with id: " + response.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusinessEntity(@PathVariable Long id) {
        logger.info("Deleting business entity with id: " + id);
        businessEntityService.deleteBusinessEntity(id);
        return ResponseEntity.ok().build();
    }
}
