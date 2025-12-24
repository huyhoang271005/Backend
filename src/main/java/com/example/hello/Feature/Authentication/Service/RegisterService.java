package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.Enum.RoleName;
import com.example.hello.Feature.Authentication.DTO.DeviceResponse;
import com.example.hello.Repository.DeviceRepository;
import com.example.hello.Repository.RoleRepository;
import com.example.hello.Repository.SessionRepository;
import com.example.hello.Entity.Device;
import com.example.hello.Entity.Email;
import com.example.hello.Entity.Role;
import com.example.hello.Entity.Session;
import com.example.hello.Enum.UserStatus;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Feature.User.DTO.RegisterRequest;
import com.example.hello.Mapper.UserMapper;
import com.example.hello.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                .ipAddress(ip)
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
