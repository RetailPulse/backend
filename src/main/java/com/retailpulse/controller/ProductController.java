package com.retailpulse.controller;

import com.retailpulse.entity.Product;
import com.retailpulse.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = Logger.getLogger(ProductController.class.getName());

    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Fetching all products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.info("Fetching product with id: " + id);
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found with id: " + id));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySKU(@PathVariable String sku) {
        logger.info("Fetching product with sku: " + sku);
        Product product = productService.getProductBySKU(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found with sku: " + sku));
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        logger.info("Received request to create product: " + product);
        try {
            Product createdProduct = productService.saveProduct(product);
            logger.info("Successfully created product with sku: " + createdProduct.getSku());
            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            logger.severe("Error creating product: " + e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        logger.info("Received request to update product with id: " + id);
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            logger.info("Successfully updated product with id: " + updatedProduct.getId());
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            logger.severe("Error updating product: " + e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product with id: " + id);
        productService.softDeleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reverseSoftDelete/{id}")
    public ResponseEntity<Product> reverseSoftDeleteProduct(@PathVariable Long id) {
        logger.info("Reverse soft delete of product with id: " + id);
        Product product = productService.reverseSoftDelete(id);
        return ResponseEntity.ok(product);
    }
}
