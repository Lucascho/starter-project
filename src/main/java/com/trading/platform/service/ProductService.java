package com.trading.platform.service;

import com.trading.platform.dto.ProductRequest;
import com.trading.platform.entity.Product;
import com.trading.platform.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(ProductRequest request) {
        Product p = new Product();
        p.setName(request.name());
        p.setPrice(request.price());
        p.setStock(request.stock());
        return productRepository.save(p);
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("商品不存在"));
        p.setName(request.name());
        p.setPrice(request.price());
        p.setStock(request.stock());
        return productRepository.save(p);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Page<Product> listProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("商品不存在"));
    }

    public Page<Product> searchByName(String keyword, Pageable pageable) {
        return productRepository.searchByName(keyword, pageable);
    }
}
