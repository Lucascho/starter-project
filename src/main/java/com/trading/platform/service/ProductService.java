package com.trading.platform.service;

import com.trading.platform.dto.ProductRequest;
import com.trading.platform.dto.ProductSearchCriteria;
import com.trading.platform.entity.Product;
import com.trading.platform.repository.ProductRepository;
import com.trading.platform.repository.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "name", "price", "stock", "createdAt"
    );
    private static final int MAX_PAGE_SIZE = 100;

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
        return searchProducts(new ProductSearchCriteria(null, null, null), pageable);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("商品不存在"));
    }

    public Page<Product> searchByName(String keyword, Pageable pageable) {
        return searchProducts(new ProductSearchCriteria(keyword, null, null), pageable);
    }

    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        validateSearchCriteria(criteria);
        validateSort(pageable);
        return productRepository.findAll(ProductSpecifications.search(criteria), pageable);
    }

    private void validateSearchCriteria(ProductSearchCriteria criteria) {
        if (criteria.minPrice() != null && criteria.maxPrice() != null
                && criteria.minPrice() > criteria.maxPrice()) {
            throw new IllegalArgumentException("最低價格不可大於最高價格");
        }
        if (criteria.minPrice() != null && criteria.minPrice() < 0) {
            throw new IllegalArgumentException("最低價格不可小於 0");
        }
        if (criteria.maxPrice() != null && criteria.maxPrice() < 0) {
            throw new IllegalArgumentException("最高價格不可小於 0");
        }
    }

    private void validateSort(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("每頁筆數不可超過 " + MAX_PAGE_SIZE);
        }
        pageable.getSort().forEach(order -> {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new IllegalArgumentException("不支援的排序欄位: " + order.getProperty());
            }
        });
    }
}
