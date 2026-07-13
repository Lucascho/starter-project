package com.trading.platform.service;

import com.trading.platform.dto.ProductSearchCriteria;
import com.trading.platform.entity.Product;
import com.trading.platform.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void searchProductsUsesSpecificationAndPageable() {
        Product product = new Product();
        product.setId(1L);
        product.setName("機械鍵盤");
        product.setPrice(2999.99);
        product.setStock(10);

        Pageable pageable = PageRequest.of(0, 20, Sort.by("price").ascending());
        ProductSearchCriteria criteria = new ProductSearchCriteria("鍵盤", 1000.0, 5000.0);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(
                anySpecification(),
                org.mockito.ArgumentMatchers.<Pageable>any()
        )).thenReturn(page);

        Page<Product> result = productService.searchProducts(criteria, pageable);

        assertThat(result.getContent()).containsExactly(product);
        verify(productRepository).findAll(
                anySpecification(),
                org.mockito.ArgumentMatchers.<Pageable>any()
        );
    }

    @Test
    void searchProductsRejectsInvalidPriceRange() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, 5000.0, 1000.0);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));

        assertThatThrownBy(() -> productService.searchProducts(criteria, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("最低價格不可大於最高價格");

        verifyNoInteractions(productRepository);
    }

    @Test
    void searchProductsRejectsNegativePrice() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, -1.0, null);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));

        assertThatThrownBy(() -> productService.searchProducts(criteria, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("最低價格不可小於 0");

        verifyNoInteractions(productRepository);
    }

    @Test
    void searchProductsRejectsUnsupportedSortProperty() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("password"));

        assertThatThrownBy(() -> productService.searchProducts(criteria, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不支援的排序欄位: password");

        verifyNoInteractions(productRepository);
    }

    @Test
    void searchProductsRejectsOversizedPage() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null);
        Pageable pageable = PageRequest.of(0, 101, Sort.by("id"));

        assertThatThrownBy(() -> productService.searchProducts(criteria, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("每頁筆數不可超過 100");

        verifyNoInteractions(productRepository);
    }

    private Specification<Product> anySpecification() {
        return any();
    }
}
