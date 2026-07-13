package com.trading.platform.service;

import com.trading.platform.dto.OrderRequest;
import com.trading.platform.entity.Order;
import com.trading.platform.entity.Product;
import com.trading.platform.entity.User;
import com.trading.platform.repository.OrderRepository;
import com.trading.platform.repository.ProductRepository;
import com.trading.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order placeOrder(String username, OrderRequest request) {
        validateOrder(request);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("使用者不存在"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSuchElementException("商品不存在"));

        int updatedRows = productRepository.decreaseStockIfAvailable(product.getId(), request.getQuantity());
        if (updatedRows == 0) {
            throw new IllegalStateException("庫存不足");
        }

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(product.getPrice() * request.getQuantity());
        return orderRepository.save(order);
    }

    private void validateOrder(OrderRequest request) {
        if (request == null || request.getProductId() == null || request.getQuantity() == null
                || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("訂單資料有誤");
        }
    }

    public List<Order> getUserOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("使用者不存在"));
        List<Order> orders = orderRepository.findByUserId(user.getId());
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getUser().getId().equals(user.getId())) {
                o.getProduct().getName();
                o.getUser().getUsername();
                result.add(o);
            }
        }
        return result;
    }
}
