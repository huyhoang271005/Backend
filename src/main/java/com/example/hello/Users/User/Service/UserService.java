package com.example.hello.Users.User.Service;

import com.example.hello.Infrastructure.Cache.RoleCache;
import com.example.hello.Infrastructure.Cache.UserStatusCacheService;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.RolePermission.Entity.Role;
import com.example.hello.RolePermission.Initializer.PermissionName;
import com.example.hello.Infrastructure.Cache.RolePermissionCacheService;
import com.example.hello.Users.User.Enum.UserStatus;
import com.example.hello.Users.User.DTO.*;
import com.example.hello.Users.User.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RolePermissionCacheService rolePermissionCacheService;
    UserStatusCacheService userStatusCacheService;
    RoleCache roleCache;
    EntityManager entityManager;


    @Transactional(readOnly = true)
    public Response<List<UserResponse>> getUsers(){
        //Lấy danh sách user
        var users = userRepository.findAll();
        //Map danh sách user vào response
        var userResponse = users.stream()
                .map(user -> {
                    var profile = user.getProfile();
                    return UserResponse.builder()
                            .userId(user.getUserId())
                            .username(user.getUsername())
                            .fullName(profile.getFullName())
                            .imageUrl(profile.getImageUrl())
                            .createdAt(profile.getCreatedAt())
                            .build();
                })
                .toList();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                userResponse
        );
    }

    @Transactional(readOnly = true)
    public Response<?> getUserStatuses(){
        //Lấy danh sách các trạng thái người dùng
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                UserStatus.values()
        );
    }

    @Transactional(readOnly = true)
    public Response<UserDetailResponse> getUser(UUID userId, UUID myId) {
        //Tìm user theo userId
        var user = userRepository.findById(userId)
                .orElseThrow(()->
                    new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST));
        //Tìm user theo Id của mình
        var mine = userRepository.findById(myId)
                .orElseThrow(()->
                new  EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.INVALID));
        //Kiểm tra xem User hiện đang truy cập có quyền xem thông tin nâng cao không
        var extendUser = rolePermissionCacheService.getPermissionsCache(mine.getRole().getRoleId()).stream()
                .filter(s -> s.equals(PermissionName.GET_USER_EXTEND.name()))
                .findFirst().orElse(null);
        ExtendUserResponse extendUserResponse = null;
        //Nếu có quyền thì sẽ Build thông tin nâng cao còn không trả null
        if(extendUser != null) {
            var role = user.getRole();
            extendUserResponse = ExtendUserResponse.builder()
                    .userStatus(user.getUserStatus())
                    .emails(user.getEmails().stream()
                            .map(userEmail ->
                                    EmailResponse.builder()
                                            .emailId(userEmail.getEmailId())
                                            .email(userEmail.getEmail())
                                            .validated(userEmail.getValidated())
                                            .build())
                            .toList())
                    .roleId(role.getRoleId())
                    .build();
        }
        //Build thông tin user trả về
        var profile = user.getProfile();
        var userDetails = UserDetailResponse.builder()
                .UserId(user.getUserId())
                .gender(profile.getGender())
                .username(user.getUsername())
                .fullName(profile.getFullName())
                .birthday(profile.getBirthday())
                .imageUrl(profile.getImageUrl())
                .createdAt(profile.getCreatedAt())
                .extendUserResponse(extendUserResponse)
                .build();
        return new Response<>(
                true,
                StringApplication.FIELD.REQUEST,
                userDetails
        );
    }

    @Transactional
    public Response<Void> updateUser(ExtendUserRequest extendUserRequest){
        //Tìm user theo userId
        var user = userRepository.findById(extendUserRequest.getUserId()).orElseThrow(
                ()-> new  EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST)
        );
        //Thay đổi các thông tin nâng cao (chức vụ, trạng thái người dùng)
        user.setUserStatus(extendUserRequest.getUserStatus());
        var userRole = entityManager.getReference(Role.class, extendUserRequest.getRoleId());
        user.setRole(userRole);
        //Lưu vào db và set vào cache
        userRepository.save(user);
        userStatusCacheService.updateUserStatus(user.getUserId(), extendUserRequest.getUserStatus());
        roleCache.putRoleCache(user.getUserId(), userRole.getRoleId());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
