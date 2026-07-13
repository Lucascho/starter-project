package com.trading.platform.service;

import com.trading.platform.dto.OrderRequest;
import com.trading.platform.entity.Order;
import com.trading.platform.entity.Product;
import com.trading.platform.entity.User;
import com.trading.platform.repository.OrderRepository;
import com.trading.platform.repository.ProductRepository;
import com.trading.platform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrderDecreasesStockAtomicallyAndKeepsDecimalPrice() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        Product product = new Product();
        product.setId(10L);
        product.setName("keyboard");
        product.setPrice(2999.99);
        product.setStock(5);

        OrderRequest request = new OrderRequest();
        request.setProductId(product.getId());
        request.setQuantity(2);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockIfAvailable(product.getId(), 2)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.placeOrder("alice", request);

        assertThat(order.getUser()).isSameAs(user);
        assertThat(order.getProduct()).isSameAs(product);
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getTotalPrice()).isEqualTo(5999.98);
        verify(productRepository).decreaseStockIfAvailable(product.getId(), 2);
    }

    @Test
    void placeOrderDoesNotCreateOrderWhenStockUpdateFails() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        Product product = new Product();
        product.setId(10L);
        product.setStock(1);

        OrderRequest request = new OrderRequest();
        request.setProductId(product.getId());
        request.setQuantity(2);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockIfAvailable(product.getId(), 2)).thenReturn(0);

        assertThatThrownBy(() -> orderService.placeOrder("alice", request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("庫存不足");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrderRejectsInvalidQuantityBeforeRepositoryAccess() {
        OrderRequest request = new OrderRequest();
        request.setProductId(10L);
        request.setQuantity(0);

        assertThatThrownBy(() -> orderService.placeOrder("alice", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("訂單資料有誤");

        verifyNoInteractions(userRepository, productRepository, orderRepository);
    }
}
