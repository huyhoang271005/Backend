package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Entity.*;
import com.example.hello.Enum.RoleName;
import com.example.hello.Enum.Gender;
import com.example.hello.Feature.Authentication.dto.DeviceResponse;
import com.example.hello.Feature.Notification.NotificationService;
import com.example.hello.Feature.RolePermission.Repository.RoleRepository;
import com.example.hello.Feature.User.Repository.DeviceRepository;
import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.Feature.User.Repository.UserRepository;
import com.example.hello.Feature.User.dto.RegisterRequest;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Mapper.UserMapper;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    UserMapper userMapper;
    @Mock
    DeviceRepository deviceRepository;
    @Mock
    SessionRepository sessionRepository;
    @Mock
    CloudinaryService cloudinaryService;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    RegisterService registerService;

    private RegisterRequest registerRequest;
    private Role role;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("Test User");
        registerRequest.setGender(Gender.MALE);

        role = Role.builder().roleName(RoleName.USER.name()).build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmails_Email(anyString())).thenReturn(false);
        
        User mockUser = new User();
        Profile mockProfile = new Profile();
        
        when(userMapper.toUser(any())).thenReturn(mockUser);
        when(userMapper.toProfile(any())).thenReturn(mockProfile);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
        
        // Mock Device
        when(deviceRepository.findById(any())).thenReturn(Optional.empty());
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> {
            Device d = i.getArgument(0);
            d.setDeviceId(UUID.randomUUID());
            return d;
        });

        Response<DeviceResponse> response = registerService.register(
                registerRequest, null, UUID.randomUUID(), "Agent", "Type", "Name", "127.0.0.1"
        );

        assertTrue(response.getSuccess());
        assertEquals(StringApplication.SUCCESS.REGISTER_SUCCESS, response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void register_EmailExists() {
        when(userRepository.existsByEmails_Email(registerRequest.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> 
            registerService.register(
                registerRequest, null, null, "Agent", "Type", "Name", "127.0.0.1"
            )
        );
    }

    @Test
    void register_UsernameExists() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(ConflictException.class, () -> 
            registerService.register(
                registerRequest, null, null, "Agent", "Type", "Name", "127.0.0.1"
            )
        );
    }
}
