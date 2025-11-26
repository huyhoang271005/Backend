package com.example.hello.Users.User.Entity;

import com.example.hello.Users.User.Enum.Gender;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference
    User user;

    @Id
    UUID userId;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "image_id")
    String imageId;

    LocalDate birthday;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    Gender gender;
}
