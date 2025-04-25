package com.retailpulse.service.report;

import com.retailpulse.DTO.ProductDto;
import com.retailpulse.controller.exception.ApplicationException;
import com.retailpulse.entity.Product;
import com.retailpulse.repository.ProductRepository;
import com.retailpulse.service.dataExtractor.ProductDataExtractor;
import com.retailpulse.service.dataExtractor.TableDataExtractor;
import com.retailpulse.service.exportReportHelper.ExcelReportExportService;
import com.retailpulse.service.exportReportHelper.PdfReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductReportService {

    private final ProductRepository productRepository;

    public ProductReportService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(p -> new ProductDto(
                        p.getSku(),
                        p.getDescription(),
                        p.getCategory(),
                        p.getSubcategory(),
                        p.getBrand()))
                .collect(Collectors.toList());
    }

    public void exportReport(HttpServletResponse response, Instant start, Instant end, String format) throws IOException {
        List<ProductDto> data = getAllProducts();
        String[] headers = new String[]{
                "S/No.", "SKU", "Description", "Category", "Subcategory", "Brand"
        };
        String title = "Product Report";

        TableDataExtractor<ProductDto> extractor = new ProductDataExtractor();
        if ("pdf".equalsIgnoreCase(format)) {
            PdfReportExportService<ProductDto> exportService = new PdfReportExportService<>(title, headers, extractor);
            exportService.exportReport(response, start, end, data);
        } else if ("excel".equalsIgnoreCase(format)) {
            ExcelReportExportService<ProductDto> exportService = new ExcelReportExportService<>(title, headers, extractor);
            exportService.exportReport(response, start, end, data);
        } else {
            throw new ApplicationException("INVALID_FORMAT", "Unsupported export format: " + format);
        }
    }
}