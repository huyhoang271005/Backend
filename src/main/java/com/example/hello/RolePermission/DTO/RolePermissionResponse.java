package com.example.hello.RolePermission.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RolePermissionResponse {
    UUID rolePermissionId;
    String permissionName;
}
