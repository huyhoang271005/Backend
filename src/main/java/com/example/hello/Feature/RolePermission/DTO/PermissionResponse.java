package com.example.hello.Feature.RolePermission.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionResponse {
    UUID permissionId;
    String permissionName;

}
