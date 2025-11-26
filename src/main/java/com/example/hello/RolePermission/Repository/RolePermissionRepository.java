package com.example.hello.RolePermission.Repository;

import com.example.hello.RolePermission.Entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    Boolean existsByPermission_PermissionIdAndRole_RoleId(UUID roleId, UUID permissionId);

    Optional<RolePermission> findByRole_RoleNameAndPermission_PermissionName(String userRoleRoleName, String permissionPermissionName);

    List<RolePermission> findByRole_RoleId(UUID roleId);
}
