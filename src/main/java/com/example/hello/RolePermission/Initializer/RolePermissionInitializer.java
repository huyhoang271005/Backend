package com.example.hello.RolePermission.Initializer;

import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.RolePermission.Entity.Permission;
import com.example.hello.RolePermission.Entity.RolePermission;
import com.example.hello.RolePermission.Entity.Role;
import com.example.hello.RolePermission.Repository.PermissionRepository;
import com.example.hello.RolePermission.Repository.RolePermissionRepository;
import com.example.hello.RolePermission.Repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class RolePermissionInitializer implements CommandLineRunner {
    PermissionRepository permissionRepository;
    RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (PermissionName permissionName : PermissionName.values()) {
            permissionRepository.findByPermissionName(permissionName.name())
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .permissionName(permissionName.name()).build()));
        }

        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByRoleName(roleName.name()).orElseGet(
                    () -> roleRepository.save(Role.builder()
                            .roleName(roleName.name()).build())
            );
        }

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN.name()).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.ROLE + StringApplication.FIELD.NOT_EXIST)
        );
        List<Permission> allPermissions = permissionRepository.findAll();
        for (Permission permission : allPermissions) {
            rolePermissionRepository.findByRole_RoleNameAndPermission_PermissionName(
                    adminRole.getRoleName(), permission.getPermissionName()
            ).orElseGet(() -> rolePermissionRepository.save(rolePermissionRepository.save(
                    RolePermission.builder()
                            .permission(permission)
                            .role(adminRole)
                            .build()
            )));
        }
    }
}
