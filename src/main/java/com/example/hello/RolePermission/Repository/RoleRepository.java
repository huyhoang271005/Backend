package com.example.hello.RolePermission.Repository;

import com.example.hello.RolePermission.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleName(String roleName);
    Boolean existsByRoleName(String roleName);
}
