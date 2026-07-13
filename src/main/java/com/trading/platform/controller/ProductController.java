package com.trading.platform.controller;

import com.trading.platform.dto.ProductRequest;
import com.trading.platform.dto.ProductResponse;
import com.trading.platform.dto.ProductSearchCriteria;
import com.trading.platform.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<ProductResponse> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Double minPrice,
                                      @RequestParam(required = false) Double maxPrice,
                                      @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(keyword, minPrice, maxPrice);
        return productService.searchProducts(criteria, pageable).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return ProductResponse.from(productService.getProduct(id));
    }

    @GetMapping("/search")
    public Page<ProductResponse> search(@RequestParam String keyword,
                                        @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return productService.searchByName(keyword, pageable).map(ProductResponse::from);
    }
}
