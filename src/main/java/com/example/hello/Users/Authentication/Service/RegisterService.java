package com.example.hello.Users.Authentication.Service;

import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.RolePermission.Initializer.RoleName;
import com.example.hello.Users.Authentication.DTO.DeviceResponse;
import com.example.hello.Users.Authentication.Repository.DeviceRepository;
import com.example.hello.RolePermission.Repository.RoleRepository;
import com.example.hello.Users.Authentication.Repository.SessionRepository;
import com.example.hello.Users.Authentication.Entity.Device;
import com.example.hello.Users.Authentication.Entity.Email;
import com.example.hello.RolePermission.Entity.Role;
import com.example.hello.Users.Authentication.Entity.Session;
import com.example.hello.Users.User.Enum.UserStatus;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Users.User.DTO.RegisterRequest;
import com.example.hello.Users.User.Mapper.UserMapper;
import com.example.hello.Users.User.Repository.UserRepository;
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

    @Transactional
    public Response<DeviceResponse> register(RegisterRequest registerRequest, MultipartFile avatar, UUID deviceId, String userAgent, String deviceType, String deviceName){
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
        var user = userMapper.toUsers(registerRequest);
        //Set Bcrypt password
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        //Map dữ liệu request sang entity profile
        var userProfile = userMapper.toUserProfile(registerRequest);
        //Nếu user gửi ảnh lên thì xử lý ảnh
        if(!avatar.isEmpty()) {
            CloudinaryResponse result = cloudinaryService.uploadImage(avatar, "user1");
            userProfile.setImageUrl(result.getUrl());
            userProfile.setImageId(result.getPublicId());
        }
        userProfile.setGender(registerRequest.getGender());
        userProfile.setUser(user);
        user.setProfile(userProfile);

        //Set entity email
        Email email = Email.builder()
                .email(registerRequest.getEmail())
                .validated(false)
                .user(user)
                .build();
        user.setEmails(List.of(email));
        user.setUserStatus(UserStatus.PENDING);

        //Set role cho user nếu không tìm thấy role này thì đã exception
        Role role = roleRepository.findByRoleName(RoleName.USER.name()).orElseThrow(
                ()-> new RuntimeException(StringApplication.ERROR.INTERNAL_SERVER_ERROR)
        );
        role.setUsers(List.of(user));
        user.setRole(role);
        userRepository.save(user);
        //Tìm device nếu không tồn tại tạo device mới và lưu
        var device = Optional.ofNullable(deviceId)
                        .flatMap(deviceRepository::findById)
                                .orElseGet(()->Device.builder()
                                        .userAgent(userAgent)
                                        .deviceName(deviceName)
                                        .deviceType(deviceType)
                                        .build());
        deviceRepository.save(device);
        //Lưu session dựa trên device và cho phép nó đăng nhập và đã được xác thực
        var session = Session.builder()
                .user(user)
                .device(device)
                .revoked(false)
                .validated(true)
                .build();
        sessionRepository.save(session);
        return new Response<>(
                true,
                StringApplication.SUCCESS.REGISTER_SUCCESS,
                new DeviceResponse(device.getDeviceId())
        );
    }
}
