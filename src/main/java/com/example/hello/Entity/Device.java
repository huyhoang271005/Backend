package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "device")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id")
    UUID deviceId;

    @Column(name = "user_agent", columnDefinition = "VARCHAR(255)")
    String userAgent;

    @Column(name = "device_name", columnDefinition = "NVARCHAR(255)")
    String deviceName;

    @Column(name = "device_type", columnDefinition = "VARCHAR(255)")
    String deviceType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "device", cascade = CascadeType.MERGE)
    List<Session> sessions;
}
