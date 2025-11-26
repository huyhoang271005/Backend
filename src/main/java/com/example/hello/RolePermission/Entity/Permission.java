package com.example.hello.RolePermission.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "permission")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id")
    UUID permissionId;

    @Column(name = "permission_name", unique = true)
    String permissionName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "permission", cascade = CascadeType.ALL)
    List<RolePermission> rolePermission;
}
