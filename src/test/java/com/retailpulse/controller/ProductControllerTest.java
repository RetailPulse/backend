package com.retailpulse.controller;

import com.retailpulse.entity.Product;
import com.retailpulse.exception.GlobalExceptionHandler;
import com.retailpulse.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductById() throws Exception {
        Product product = new Product();
        product.setId(1L);
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testCreateProductException() throws Exception {
        when(productService.saveProduct(any(Product.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": \"error\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetProductBySKUSuccess() throws Exception {
        Product product = new Product();
        product.setSku("sku123");
        when(productService.getProductBySKU("sku123")).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/sku/sku123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("sku123"));
    }

    @Test
    void testGetProductBySKUNotFound() throws Exception {
        when(productService.getProductBySKU("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/sku/unknown"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateProduct() throws Exception {
        Product product = new Product();
        product.setSku("12345");
        when(productService.saveProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": \"12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("12345"));
    }

    @Test
    void testUpdateProduct() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setSku("12345");
        when(productService.updateProduct(anyLong(), any(Product.class))).thenReturn(product);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": \"12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("12345"));
    }

    @Test
    void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());

        verify(productService, times(1)).softDeleteProduct(1L);
    }

    @Test
    void testReverseSoftDeleteProduct() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setSku("skuRevived");

        when(productService.reverseSoftDelete(1L)).thenReturn(product);

        mockMvc.perform(put("/api/products/reverseSoftDelete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sku").value("skuRevived"));
    }
}



