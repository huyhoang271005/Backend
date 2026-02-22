package com.example.hello.Entity;

import com.example.hello.Enum.OrderStatus;
import com.example.hello.Enum.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "[order]", indexes = {
        @Index(name = "idx_order_user_id", columnList = "user_id"),
        @Index(name = "idx_order_created_at", columnList = "created_at"),
        @Index(name = "idx_order_updated_at", columnList = "updated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    UUID orderId;

    @Column(name = "contact_name", columnDefinition = "NVARCHAR(255)")
    String contactName;

    @Column(columnDefinition = "VARCHAR(20)")
    String phone;

    @Column(columnDefinition = "NVARCHAR(255)")
    String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    OrderStatus orderStatus;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @Column(name = "payment_at")
    Instant paymentAt;

    @Column(name = "payment_id", columnDefinition = "VARCHAR(255)")
    String paymentId;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderItem> orderItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
    List<Feedback> feedbacks;
}
