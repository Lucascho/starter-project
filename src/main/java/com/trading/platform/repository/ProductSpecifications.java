package com.trading.platform.repository;

import com.trading.platform.dto.ProductSearchCriteria;
import com.trading.platform.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> search(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            String keyword = criteria.normalizedKeyword();

            if (keyword != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword + "%"));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
