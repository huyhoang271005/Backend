package com.example.hello.Feature.RolePermission.Service;

import com.example.hello.Feature.RolePermission.DTO.*;
import com.example.hello.Infrastructure.Cache.RolePermissionCacheService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Entity.Permission;
import com.example.hello.Entity.RolePermission;
import com.example.hello.Entity.Role;
import com.example.hello.Enum.PermissionName;
import com.example.hello.Enum.RoleName;
import com.example.hello.Repository.PermissionRepository;
import com.example.hello.Repository.RolePermissionRepository;
import com.example.hello.Repository.RoleRepository;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionService {
    RolePermissionRepository rolePermissionRepository;
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RolePermissionCacheService rolePermissionCacheService;
    EntityManager entityManager;

    @Transactional
    public Response<RolePermissionResponse> addRolePermission(RolePermissionRequest rolePermissionRequest) {
        //Kiểm tra xem quyền cho chức vụ này tồn tại chưa
        var oldRolePermission = rolePermissionRepository.existsByPermission_PermissionIdAndRole_RoleId
                (rolePermissionRequest.getRoleId(), rolePermissionRequest.getPermissionId());
        if (oldRolePermission) {
            //Nếu tồn tại đá exception
            throw new ConflictException(StringApplication.FIELD.ROLE_PERMISSION + StringApplication.FIELD.EXISTED);
        }
        //Tạo quyền mới cho chức vụ này
        var rolePermission = RolePermission.builder()
                .permission(entityManager.getReference(Permission.class, rolePermissionRequest.getPermissionId()))
                .role(entityManager.getReference(Role.class,  rolePermissionRequest.getRoleId()))
                .build();
        log.info("Role permission successfully added");
        rolePermissionRepository.save(rolePermission);
        //Lưu vào cache chức vụ này
        rolePermissionCacheService.invalidatePermissionCache(rolePermission.getRole().getRoleId());
        log.info("Role permission cache successfully added");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                RolePermissionResponse.builder()
                        .rolePermissionId(rolePermission.getRolePermissionId())
                        .permissionName(rolePermission.getPermission().getPermissionName())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Response<List<UserRoleResponse>> getRolePermission() {
        //Tìm tất cả quyền cho các chức vụ và map và response
        var userRole = roleRepository.findAll();
        log.info("Found all role permissions successfully");
        var userRoleResponse = userRole.stream()
                .map(userRoleEntity -> new UserRoleResponse(
                        userRoleEntity.getRoleId(),
                        userRoleEntity.getRoleName(),
                        userRoleEntity.getRolePermission().stream()
                                .map(rolePermission -> new RolePermissionResponse(
                                        rolePermission.getRolePermissionId(),
                                        rolePermission.getPermission().getPermissionName()
                                )).toList()
                ))
                .toList();
        return new Response<>(true,
                StringApplication.FIELD.SUCCESS, 
                userRoleResponse);
    }

    @Transactional
    public  Response<Void> deleteRolePermission(UUID rolePermissionId) {
        //Kiểm tra quyền cho chức vụ này tồn tại không
        var rolePermission = rolePermissionRepository.findById(rolePermissionId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.ROLE_PERMISSION + StringApplication.FIELD.NOT_EXIST)
        );
        //Nếu là các quyền và chức vụ bên dưới thì không được phép xoá
        if(rolePermission.getRole().getRoleName().equals(RoleName.ADMIN.name())
                &&(rolePermission.getPermission().getPermissionName().equals(PermissionName.DELETE_ROLE_PERMISSION.name())
                || rolePermission.getPermission().getPermissionName().equals(PermissionName.ADD_ROLE_PERMISSION.name()))
                || rolePermission.getPermission().getPermissionName().equals(PermissionName.GET_ROLE_PERMISSION.name())
                || rolePermission.getPermission().getPermissionName().equals(PermissionName.GET_PERMISSION.name())) {
            log.error("Cant delete role permission {}", rolePermission.getPermission().getPermissionName());
            throw new ConflictException(StringApplication.FIELD.CANT_REMOVE);
        }
        //Xoá quyền của chức vụ này
        rolePermissionRepository.deleteById(rolePermissionId);
        log.info("Role permission successfully deleted");
        //Xoá chức vụ này trong cache
        rolePermissionCacheService.invalidatePermissionCache(rolePermission.getRole().getRoleId());
        log.info("Role permission cache successfully deleted");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null);
    }

    @Transactional(readOnly = true)
    public Response<List<RoleResponse>> getRole() {
        //Trả về danh sách các chức vụ
        log.info("Found all roles successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                roleRepository.findAll().stream()
                        .map(userRole ->
                                new RoleResponse(userRole.getRoleId(), userRole.getRoleName()))
                        .toList()
        );
    }

    @Transactional
    public Response<UserRoleResponse> addRole (String roleName){
        roleName = roleName.toUpperCase();
        //Kiểm tra tên chức vụ này đã tồn tại chưa
        var role = roleRepository.existsByRoleName(roleName);
        if(role){
            throw new ConflictException(StringApplication.FIELD.ROLE + StringApplication.FIELD.EXISTED);
        }
        //Lưu vào db
        var userRole = Role.builder()
                .roleName(roleName)
                .build();
        roleRepository.save(userRole);
        log.info("Role successfully added");
        rolePermissionCacheService.invalidatePermissionCache(userRole.getRoleId());
        log.info("Role cache successfully added");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                UserRoleResponse.builder()
                        .roleId(userRole.getRoleId())
                        .roleName(roleName)
                        .build()
        );
    }

    @Transactional
    public Response<Void> deleteRole (UUID roleId) {
        //Kiểm tra role này tồn tại không
        var userRole = roleRepository.findById(roleId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.ROLE + StringApplication.FIELD.NOT_EXIST)
        );
        //Kiểm tra nếu thuộc 2 role bên dưới thì không xoá được
        if(userRole.getRoleName().equals(RoleName.ADMIN.name()) || userRole.getRoleName().equals(RoleName.USER.name())) {
            log.error("Cant delete role {}", userRole.getRoleName());
            throw new ConflictException(StringApplication.FIELD.CANT_REMOVE);
        }
        //Xoá role
        roleRepository.deleteById(roleId);
        log.info("Role successfully deleted");
        //Giải phóng role khỏi cache
        rolePermissionCacheService.invalidatePermissionCache(userRole.getRoleId());
        log.info("Role cache successfully deleted");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<List<PermissionResponse>> getAllPermissions() {
        //Lấy tất cả các quyền tồn tại
        var permissions = permissionRepository.findAll().stream()
                .map(permission -> new PermissionResponse(
                        permission.getPermissionId(), permission.getPermissionName()
                )).toList();
        log.info("Found all permissions successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                permissions
        );
    }
}