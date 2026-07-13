package com.trading.platform.repository;

import com.trading.platform.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Product p
               SET p.stock = p.stock - :quantity
             WHERE p.id = :productId
               AND p.stock >= :quantity
            """)
    int decreaseStockIfAvailable(@Param("productId") Long productId, @Param("quantity") int quantity);

    Page<Product> findAll(Pageable pageable);
}
