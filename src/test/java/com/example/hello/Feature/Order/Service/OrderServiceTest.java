package com.example.hello.Feature.Order.Service;

import com.example.hello.Entity.*;
import com.example.hello.Enum.PaymentMethod;
import com.example.hello.Feature.Order.dto.OrderDTO;
import com.example.hello.Feature.Order.dto.OrderItemDTO;
import com.example.hello.Feature.Order.Repository.OrderRepository;
import com.example.hello.Feature.Contact.ContactRepository;
import com.example.hello.Feature.User.Repository.UserRepository;
import com.example.hello.Feature.ProductsManager.Repository.VariantRepository;
import com.example.hello.Feature.Cart.CartItemRepository;
import com.example.hello.Feature.ProductsManager.Repository.VariantValueRepository;
import com.example.hello.Feature.ProductsManager.Repository.ProductRepository;
import com.example.hello.Feature.Order.Repository.OrderItemRepository;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.OrderMapper;
import com.example.hello.Middleware.Response;
import com.example.hello.SseEmitter.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    ContactRepository contactRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    VariantRepository variantRepository;
    @Mock
    CartItemRepository cartItemRepository;
    @Mock
    VariantValueRepository variantValueRepository;
    @Mock
    OrderMapper orderMapper;
    @Mock
    SseService sseService;
    @Mock
    ProductRepository productRepository;
    @Mock
    OrderItemRepository orderItemRepository;

    @InjectMocks
    OrderService orderService;

    private User user;
    private Contact contact;
    private Variant variant;
    private OrderDTO orderDTO;
    private UUID userId;
    private UUID contactId;
    private UUID variantId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        contactId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        user = User.builder().userId(userId).build();
        contact = Contact.builder().contactId(contactId).build();
        
        variant = new Variant();
        variant.setVariantId(variantId);
        variant.setPrice(BigDecimal.valueOf(100));
        variant.setStock(10);
        variant.setSold(0);

        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setVariantId(variantId);
        itemDTO.setQuantity(2);

        orderDTO = new OrderDTO();
        orderDTO.setContactId(contactId);
        orderDTO.setPaymentMethod(PaymentMethod.COD);
        orderDTO.setOrderItemDTOList(List.of(itemDTO));
    }

    @Test
    void addOrder_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));
        when(variantRepository.findAllById(anyList())).thenReturn(List.of(variant));
        
        Order mockOrder = new Order();
        mockOrder.setOrderId(UUID.randomUUID());
        when(orderMapper.toOrder(contact)).thenReturn(mockOrder);
        when(orderMapper.toOrderItem(any())).thenReturn(new OrderItem());
        
        // Mock save
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Response response = orderService.addOrder(userId, orderDTO);

        assertTrue(response.getSuccess());
        verify(orderRepository).save(any(Order.class));
        // verify(variantRepository).saveAll(anyList()); // Relies on JPA dirty checking
        assertEquals(8, variant.getStock()); // 10 - 2
        assertEquals(2, variant.getSold()); // 0 + 2
    }

    @Test
    void addOrder_StockExceeded() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));
        
        variant.setStock(1); // Less than requested 2
        when(variantRepository.findAllById(anyList())).thenReturn(List.of(variant));
        
        Order mockOrder = new Order();
        when(orderMapper.toOrder(contact)).thenReturn(mockOrder);

        assertThrows(ConflictException.class, () -> 
            orderService.addOrder(userId, orderDTO)
        );
    }

    @Test
    void addOrder_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            orderService.addOrder(userId, orderDTO)
        );
    }
}
