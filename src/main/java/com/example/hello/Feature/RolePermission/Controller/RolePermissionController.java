package com.example.hello.Feature.RolePermission.Controller;

import com.example.hello.Feature.RolePermission.dto.RolePermissionRequest;
import com.example.hello.Feature.RolePermission.dto.RoleRequest;
import com.example.hello.Feature.RolePermission.Service.RolePermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionController {
    RolePermissionService rolePermissionService;

    @PreAuthorize("hasAuthority('GET_PERMISSION')")
    @GetMapping("permissions")
    public ResponseEntity<?> getAllRolePermissions() {
        return ResponseEntity.ok(rolePermissionService.getAllPermissions());
    }

    @PreAuthorize("hasAuthority('ADD_ROLE_PERMISSION')")
    @PostMapping("role-permission")
    public ResponseEntity<?> addRolePermission(@RequestBody RolePermissionRequest rolePermissionRequest) {
        return ResponseEntity.ok(rolePermissionService.addRolePermission(rolePermissionRequest));
    }

    @PreAuthorize("hasAuthority('DELETE_ROLE_PERMISSION')")
    @DeleteMapping("role-permission/{rolePermissionId}")
    public ResponseEntity<?> deleteRolePermission(@PathVariable UUID rolePermissionId) {
        return ResponseEntity.ok(rolePermissionService.deleteRolePermission(rolePermissionId));
    }

    @PreAuthorize("hasAuthority('GET_ROLE_PERMISSION')")
    @GetMapping("role-permission")
    public ResponseEntity<?> getRolePermission() {
        return ResponseEntity.ok(rolePermissionService.getRolePermission());
    }

    @PreAuthorize("hasAuthority('GET_ROLE')")
    @GetMapping("roles")
    public ResponseEntity<?> getRole(){
        return ResponseEntity.ok(rolePermissionService.getRole());
    }

    @PreAuthorize("hasAuthority('ADD_ROLE')")
    @PostMapping("roles")
    public ResponseEntity<?>  addRole(@RequestBody RoleRequest roleRequest) {
        return ResponseEntity.ok(rolePermissionService.addRole(roleRequest.getRoleName()));
    }

    @PreAuthorize("hasAuthority('DELETE_ROLE')")
    @DeleteMapping("roles/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable UUID roleId) {
        return ResponseEntity.ok(rolePermissionService.deleteRole(roleId));
    }
}
