package com.example.hello.Feature.RolePermission.Repository;

import com.example.hello.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Boolean existsByPermissionName(String permissionName);
}
