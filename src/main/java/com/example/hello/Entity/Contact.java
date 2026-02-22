package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contact", indexes = {
        @Index(name = "idx_contact_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contact_id")
    UUID contactId;

    @Column(name = "contact_name", columnDefinition = "NVARCHAR(255)")
    String contactName;

    @Column(columnDefinition = "VARCHAR(20)")
    String phone;

    @Column(columnDefinition = "NVARCHAR(255)")
    String address;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}
