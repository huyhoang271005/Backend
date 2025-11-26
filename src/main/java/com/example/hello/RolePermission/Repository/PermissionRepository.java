package com.example.hello.RolePermission.Repository;

import com.example.hello.RolePermission.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByPermissionName(String permissionName);
}
