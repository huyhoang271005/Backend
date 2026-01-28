package com.example.hello.Feature.RolePermission.Initializer;

import com.example.hello.Entity.RoomChat;
import com.example.hello.Enum.PermissionName;
import com.example.hello.Enum.RoleName;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Entity.Permission;
import com.example.hello.Entity.RolePermission;
import com.example.hello.Entity.Role;
import com.example.hello.Repository.*;
import com.example.hello.WebSocket.RoomChat.RoomChatName;
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
    RoleRepository roleRepository;
    RoomChatRepository roomChatRepository;


    @Override
    public void run(String... args) {
        for (PermissionName permissionName : PermissionName.values()) {
            if(!permissionRepository.existsByPermissionName(permissionName.name())){
                permissionRepository.save(Permission.builder()
                        .permissionName(permissionName.name()).build());
            }
        }

        for (RoleName roleName : RoleName.values()) {
            if(!roleRepository.existsByRoleName(roleName.name())){
                roleRepository.save(Role.builder()
                        .roleName(roleName.name()).build());
            }
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
        var roomChat = roomChatRepository.existsByRoomName(RoomChatName.GLOBAL.name());
        if(!roomChat){
            roomChatRepository.save(RoomChat.builder()
                    .roomName(RoomChatName.GLOBAL.name())
                    .build());
        }
    }
}
