package com.example.hello.RolePermission.Entity;

import com.example.hello.Users.User.Entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id")
    UUID roleId;

    @Column(name = "role_name", unique = true)
    String roleName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "role", cascade = CascadeType.MERGE)
    List<User> users;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "role", cascade = CascadeType.ALL)
    List<RolePermission> rolePermission;
}
