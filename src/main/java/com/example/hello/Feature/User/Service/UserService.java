package com.example.hello.Feature.User.Service;

import com.example.hello.Feature.User.dto.*;
import com.example.hello.Infrastructure.Cache.RoleCacheService;
import com.example.hello.Infrastructure.Cache.UserStatusCacheService;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.HomeMapper;
import com.example.hello.Infrastructure.Common.dto.ListResponse;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import com.example.hello.Infrastructure.Common.dto.Response;
import com.example.hello.Entity.Role;
import com.example.hello.Enum.PermissionName;
import com.example.hello.Infrastructure.Cache.RolePermissionCacheService;
import com.example.hello.Enum.UserStatus;
import com.example.hello.Feature.Cart.CartItemRepository;
import com.example.hello.Feature.Message.Repository.StatusRepository;
import com.example.hello.Feature.Notification.Repository.UserNotificationRepository;
import com.example.hello.Feature.User.Repository.UserRepository;
import com.example.hello.Feature.Message.dto.MessageStatus;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    CartItemRepository cartItemRepository;
    UserNotificationRepository userNotificationRepository;
    HomeMapper homeMapper;
    RolePermissionCacheService rolePermissionCacheService;
    UserStatusCacheService userStatusCacheService;
    RoleCacheService roleCacheService;
    EntityManager entityManager;
    StatusRepository statusRepository;
    @Qualifier("applicationTaskExecutor")
    AsyncTaskExecutor applicationTaskExecutor;


    @Transactional(readOnly = true)
    public Response<ListResponse<UserResponse>> getUsers(String email, Pageable pageable) {
        //Lấy danh sách user
        //Map danh sách user vào response
        log.info("email is {} ", email);
        var listUser = userRepository.getListUser(email, pageable);
        log.info("Users found successfully");
        Boolean hasMore = listUser.hasNext();
        var userResponse = listUser.getContent().stream()
                .map(user -> UserResponse.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .imageUrl(user.getImageUrl())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(hasMore, userResponse)
        );
    }

    @Transactional(readOnly = true)
    public Response<?> getUserStatuses(){
        //Lấy danh sách các trạng thái người dùng
        log.info("Users status found successfully");
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
        log.info("User found successfully");
        //Tìm user theo Id của mình
        var mine = userRepository.findById(myId)
                .orElseThrow(()->
                new  EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.INVALID));
        log.info("User my id found successfully");
        //Kiểm tra xem User hiện đang truy cập có quyền xem thông tin nâng cao không
        var extendUser = rolePermissionCacheService.getPermissionsCache(mine.getRole().getRoleId()).stream()
                .filter(s -> s.equals(PermissionName.GET_USER_ADMIN.name()))
                .findFirst().orElse(null);
        ExtendUserResponse extendUserResponse = null;
        //Nếu có quyền thì sẽ Build thông tin nâng cao còn không trả null
        if(extendUser != null) {
            log.info("User can get extend user");
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
        log.info("User details found successfully");
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
        log.info("User updated successfully");
        userStatusCacheService.updateUserStatus(user.getUserId(), extendUserRequest.getUserStatus());
        log.info("User status cache updated successfully");
        roleCacheService.putRoleCache(user.getUserId(), userRole.getRoleId());
        log.info("Role cache updated successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<HomeResponse> getHome(UUID userId){
        var homeInfoCompletable = CompletableFuture.supplyAsync(() ->
                userRepository.getHomeInfo(userId), applicationTaskExecutor);
        var cartItemCountCompletable = CompletableFuture.supplyAsync(() ->
                cartItemRepository.countByCart_User_UserId(userId), applicationTaskExecutor);
        var userNotificationCountCompletable = CompletableFuture.supplyAsync(() ->
                userNotificationRepository.countByUser_UserIdAndIsRead(userId, false),  applicationTaskExecutor);
        var messageCountCompletable = CompletableFuture.supplyAsync(() ->
                statusRepository.countByUser_UserIdAndMessageStatus(userId, MessageStatus.SENT), applicationTaskExecutor);
        CompletableFuture.allOf(homeInfoCompletable, cartItemCountCompletable, userNotificationCountCompletable, messageCountCompletable)
                .join();
        var homeInfo = homeInfoCompletable.join();
        log.info("User home found successfully");
        var homeResponse = homeMapper.toHomeResponse(homeInfo);
        homeResponse.setCartsCount(cartItemCountCompletable.join());
        log.info("Cart count successfully {}", homeResponse.getCartsCount());
        homeResponse.setReadNotifications(userNotificationCountCompletable.join());
        log.info("Notifications count successfully {}", homeResponse.getReadNotifications());
        homeResponse.setReadMessages(messageCountCompletable.join());
        log.info("Messages count successfully {}", homeResponse.getReadMessages());
        homeResponse.setAppName(StringApplication.FIELD.APP_NAME);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                homeResponse
        );
    }
}
