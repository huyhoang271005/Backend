package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Feature.Notification.NotificationDTO;
import com.example.hello.Feature.Notification.NotificationService;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.Enum.RoleName;
import com.example.hello.Feature.Authentication.dto.DeviceResponse;
import com.example.hello.Infrastructure.Cloudinary.FolderCloudinary;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Feature.User.Repository.DeviceRepository;
import com.example.hello.Feature.RolePermission.Repository.RoleRepository;
import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.Entity.Device;
import com.example.hello.Entity.Email;
import com.example.hello.Entity.Role;
import com.example.hello.Entity.Session;
import com.example.hello.Enum.UserStatus;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Feature.User.dto.RegisterRequest;
import com.example.hello.Mapper.UserMapper;
import com.example.hello.Feature.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegisterService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper  userMapper;
    DeviceRepository deviceRepository;
    SessionRepository sessionRepository;
    CloudinaryService  cloudinaryService;
    NotificationService notificationService;

    @Transactional
    public Response<DeviceResponse> register(RegisterRequest registerRequest,
                                             MultipartFile avatar, UUID deviceId, String userAgent,
                                             String deviceType, String deviceName, String ip){
        //Check sự tồn tại của username
        var usernameDb = userRepository.existsByUsername(registerRequest.getUsername());
        //Check sự tồn tại của email
        var emailDb = userRepository.existsByEmails_Email(registerRequest.getEmail());
        if(emailDb) {
            throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.EXISTED);
        }
        if(usernameDb) {
            throw new ConflictException(StringApplication.FIELD.USERNAME + StringApplication.FIELD.EXISTED);
        }
        //Map dữ liệu từ request sang user
        var user = userMapper.toUser(registerRequest);
        //Set Bcrypt password
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        //Map dữ liệu request sang entity profile
        var userProfile = userMapper.toProfile(registerRequest);
        //Nếu user gửi ảnh lên thì xử lý ảnh
        if(avatar != null) {
            CloudinaryResponse result = cloudinaryService.uploadImage(avatar, FolderCloudinary.user.name());
            userProfile.setImageUrl(result.getUrl());
            userProfile.setImageId(result.getPublicId());
            log.info("Image uploaded successfully");
        }
        userProfile.setGender(registerRequest.getGender());
        userProfile.setUser(user);
        user.setProfile(userProfile);
        log.info("Profile uploaded successfully");
        //Set entity email
        Email email = Email.builder()
                .email(registerRequest.getEmail())
                .validated(false)
                .user(user)
                .build();
        user.setEmails(List.of(email));
        user.setUserStatus(UserStatus.PENDING);
        log.info("User status is PENDING");

        //Set role cho user nếu không tìm thấy role này thì đã exception
        Role role = roleRepository.findByRoleName(RoleName.USER.name()).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.ROLE +
                        StringApplication.FIELD.EXISTED)
        );
        log.info("Role set is USER");
        user.setRole(role);
        userRepository.save(user);
        //Tìm device nếu không tồn tại tạo device mới và lưu
        notificationService.sendNotification(List.of(user),
                NotificationDTO.builder()
                        .title(StringApplication.NOTIFICATION.WELCOME_TITLE)
                        .message(StringApplication.NOTIFICATION.WELCOME_MESSAGE0 +
                                userProfile.getFullName() +
                                StringApplication.NOTIFICATION.WELCOME_MESSAGE1)
                        .build());
        var device = Optional.ofNullable(deviceId)
                        .flatMap(deviceRepository::findById)
                                .orElseGet(()->Device.builder()
                                        .userAgent(userAgent)
                                        .deviceName(deviceName)
                                        .deviceType(deviceType)
                                        .build());
        deviceRepository.save(device);
        log.info("Find or created device");
        //Lưu session dựa trên device và cho phép nó đăng nhập và đã được xác thực
        var session = Session.builder()
                .user(user)
                .device(device)
                .ipAddress(ip)
                .revoked(false)
                .validated(true)
                .build();
        log.info("Session created successfully");
        sessionRepository.save(session);
        return new Response<>(
                true,
                StringApplication.SUCCESS.REGISTER_SUCCESS,
                new DeviceResponse(device.getDeviceId(), session.getSessionId())
        );
    }
}
