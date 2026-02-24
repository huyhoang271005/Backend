package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Entity.*;
import java.util.List;

import com.example.hello.Feature.Authentication.dto.LoginRequest;
import com.example.hello.Feature.Authentication.dto.LoginResponse;
import com.example.hello.Feature.Authentication.UserDetail.MyUserDetails;
import com.example.hello.Feature.Authentication.Repository.TokenRepository;
import com.example.hello.Feature.User.Repository.DeviceRepository;
import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.Feature.User.dto.Address;
import com.example.hello.WebSocket.RoomChat.UserRoomChatRepository;
import com.example.hello.WebSocket.RoomChat.RoomChatRepository;
import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Jwt.JwtComponent;
import com.example.hello.Infrastructure.Jwt.JwtProperties;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    DeviceRepository deviceRepository;
    @Mock
    SessionRepository sessionRepository;
    @Mock
    SessionCacheService sessionCacheService;
    @Mock
    JwtComponent jwtComponent;
    @Mock
    JwtProperties jwtProperties;
    @Mock
    UserRoomChatRepository userRoomChatRepository;
    @Mock
    RoomChatRepository roomChatRepository;

    @InjectMocks
    LoginService loginService;

    private LoginRequest loginRequest;
    private User user;
    private Session session;
    private Device device;
    private UUID deviceId;
    private UUID sessionId;
    private Address address;
    private String deviceName;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        deviceId = UUID.randomUUID();
        sessionId = UUID.randomUUID();

        deviceName = "Chrome Android";

        address = Address.builder()
                .region("Hanoi")
                .city("Hanoi")
                .country("VN")
                .timezone("Asia/Bangkok")
                .build();

        user = User.builder()
                .userId(UUID.randomUUID())
                .emails(List.of(Email.builder().email("test@example.com").build()))
                .build();
        user.getEmails().get(0).setUser(user);

        device = Device.builder().deviceId(deviceId).build();
        
        session = Session.builder()
                .sessionId(sessionId)
                .device(device)
                .user(user)
                .validated(true)
                .revoked(false)
                .build();
    }

    @Test
    void login_Success() {
        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        MyUserDetails userDetails = mock(MyUserDetails.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.email()).thenReturn(user.getEmails().get(0));

        // Mock Session/Device
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(sessionRepository.findByUserAndDevice(user, device)).thenReturn(Optional.of(session));
        
        // Mock JWT
        when(jwtProperties.getRefreshTokenSeconds()).thenReturn(3600L);
        when(jwtProperties.getAccessTokenSeconds()).thenReturn(3600L);
        when(jwtComponent.generateToken(any(), any(), any(), any())).thenReturn("mockToken");

        // Mock Token Repo
        when(tokenRepository.findBySessionAndTokenName(any(), any())).thenReturn(Optional.empty());

        // Mock RoomChat
        when(userRoomChatRepository.existsByRoomChat_RoomNameAndUser(anyString(), any())).thenReturn(true);

        Response<LoginResponse> response = loginService.login(loginRequest, null, deviceId, address, deviceName);

        assertTrue(response.getSuccess());
        assertEquals(StringApplication.SUCCESS.LOGIN_SUCCESS, response.getMessage());
        assertNotNull(response.getData().getAccessToken());
        assertNotNull(response.getData().getRefreshToken());
    }

    @Test
    void login_BadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(ConflictException.class, () -> 
            loginService.login(loginRequest, null, deviceId, address, deviceName)
        );
    }

    @Test
    void login_NewDevice() {
        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        MyUserDetails userDetails = mock(MyUserDetails.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.email()).thenReturn(user.getEmails().get(0));

        // Mock Device Found but No Session
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(sessionRepository.findByUserAndDevice(user, device)).thenReturn(Optional.empty());

        Response<LoginResponse> response = loginService.login(loginRequest, null, deviceId, address, deviceName);

        assertTrue(response.getSuccess());
        assertEquals(StringApplication.ERROR.NEW_DEVICE, response.getMessage());
        assertFalse(response.getData().getVerifiedDevice());
    }
}
