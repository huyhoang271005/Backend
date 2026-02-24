package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Entity.*;
import com.example.hello.Enum.*;
import com.example.hello.Feature.Authentication.Repository.TokenRepository;
import com.example.hello.Feature.Notification.NotificationDTO;
import com.example.hello.Feature.Notification.NotificationService;
import com.example.hello.Feature.RolePermission.Repository.RoleRepository;
import com.example.hello.Feature.User.Repository.DeviceRepository;
import com.example.hello.Feature.User.Repository.EmailRepository;
import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.Feature.User.Repository.UserRepository;
import com.example.hello.Feature.User.dto.Address;
import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Email.EmailSenderService;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Jwt.JwtComponent;
import com.example.hello.Infrastructure.Jwt.JwtProperties;
import com.example.hello.Infrastructure.Security.AppProperties;
import com.example.hello.Mapper.SessionMapper;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.WebSocket.RoomChat.RoomChatName;
import com.example.hello.WebSocket.RoomChat.RoomChatRepository;
import com.example.hello.WebSocket.RoomChat.UserRoomChatRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class Oauth2LoginSuccessHandle extends SimpleUrlAuthenticationSuccessHandler {
    JwtComponent jwtComponent;
    JwtProperties jwtProperties;
    EmailRepository emailRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    DeviceRepository deviceRepository;
    SessionRepository sessionRepository;
    SessionCacheService sessionCacheService;
    TokenRepository tokenRepository;
    AppProperties appProperties;
    UserRoomChatRepository userRoomChatRepository;
    RoomChatRepository roomChatRepository;
    SessionMapper sessionMapper;
    EmailSenderService emailSenderService;
    NotificationService notificationService;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            log.info("Oauth2 email is {}", email);
            String name = oAuth2User.getAttribute("name");
            log.info("Oauth2 name is {}", name);
            String picture = oAuth2User.getAttribute("picture");
            log.info("Oauth2 picture is {}", picture);
            var myCookie = WebUtils.getCookie(request, ParamName.DEVICE_ID_COOKIE);
            UUID deviceId = null;
            if(myCookie != null) {
                log.info("Device id cookie is {}", myCookie.getValue());
                deviceId = UUID.fromString(myCookie.getValue());
            }
            else {
                log.warn("Device id cookie is null");
            }

            boolean newUser = false;
            var userOptional = userRepository.findByEmails_Email(email);
            User user;
            if(userOptional.isEmpty()) {
                newUser = true;
                var profile = Profile.builder()
                        .birthday(LocalDate.now())
                        .fullName(name)
                        .gender(Gender.FEMALE)
                        .imageUrl(picture)
                        .build();
                var role = roleRepository.findByRoleName(RoleName.USER.name()).orElseThrow(() ->
                        new EntityNotFoundException(StringApplication.FIELD.ROLE
                                + StringApplication.FIELD.NOT_EXIST));
                log.info("Oauth2 created new user");
                var currentUser = User.builder()
                        .username(UUID.randomUUID().toString())
                        .password(UUID.randomUUID().toString())
                        .role(role)
                        .userStatus(UserStatus.ACTIVE)
                        .profile(profile)
                        .build();
                profile.setUser(currentUser);
                userRepository.save(currentUser);
                var userEmail = Email.builder()
                        .email(email)
                        .validated(true)
                        .user(currentUser)
                        .build();
                emailRepository.save(userEmail);
                user = currentUser;
                emailSenderService.sendEmailWelcome(email, name);
                notificationService.sendNotification(List.of(user),
                        NotificationDTO.builder()
                                .title(StringApplication.NOTIFICATION.WELCOME_TITLE)
                                .message(StringApplication.NOTIFICATION.WELCOME_MESSAGE0 +
                                        user.getProfile().getFullName() +
                                        StringApplication.NOTIFICATION.WELCOME_MESSAGE1)
                                .build());
            }
            else {
                user = userOptional.get();
            }
            var deviceName = (String) request.getAttribute(ParamName.DEVICE_NAME_ATTRIBUTE);
            var deviceType = (DeviceType) request.getAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE);
            var userAgent = request.getHeader(HttpHeaders.USER_AGENT);
            var device = Optional.ofNullable(deviceId)
                    .flatMap(deviceRepository::findById)
                    .orElseGet(() ->
                    {
                        log.info("Created new device");
                        return  Device.builder()
                                .deviceName(deviceName)
                                .deviceType(deviceType.name())
                                .userAgent(userAgent)
                                .build();
                    });
            deviceRepository.save(device);
            var ipAddress = (String) request.getAttribute(ParamName.IP_ADDRESS_ATTRIBUTE);
            var address = (Address) request.getAttribute(ParamName.ADDRESS_ATTRIBUTE);
            var sessionOptional = sessionRepository.findByUserAndDevice(user, device);
            Session session;
            if(sessionOptional.isEmpty()){
                session = Session.builder()
                        .user(user)
                        .device(device)
                        .ipAddress(ipAddress)
                        .validated(true)
                        .build();
                if(!newUser){
                    user.getEmails().stream()
                            .map(Email::getEmail)
                            .forEach(s -> emailSenderService.sendEmailWarningDevice(s,
                                    user.getProfile().getFullName(),
                                    address, deviceName));
                    notificationService.sendNotification(List.of(user),
                            NotificationDTO.builder()
                                    .title(StringApplication.NOTIFICATION.WARNING_TITLE)
                                    .message(StringApplication.NOTIFICATION.WARNING_DEVICE_MESSAGE)
                                    .linkUrl(appProperties.getFrontendUrl() + "/session")
                                    .build());
                }
            }
            else {
                session = sessionOptional.get();
            }
            session.setRevoked(false);
            session.setLastLogin(Instant.now());
            sessionMapper.updateSession(address, session);
            sessionRepository.save(session);
            sessionCacheService.updateRevoked(session.getSessionId(), false);
            var refreshToken = jwtComponent.generateToken(user.getUserId(),
                    TokenName.REFRESH_TOKEN, session.getSessionId(),
                    Instant.now().plusSeconds(jwtProperties.getRefreshTokenSeconds()));
            var userToken = tokenRepository.findBySessionAndTokenName(session, TokenName.REFRESH_TOKEN)
                    .orElseGet(() ->
                            Token.builder()
                                    .session(session)
                                    .tokenName(TokenName.REFRESH_TOKEN)
                                    .build()
                    );
            userToken.setTokenValue(refreshToken);
            tokenRepository.save(userToken);
            ResponseCookie tokenCookie = ResponseCookie.from(ParamName.REFRESH_TOKEN_COOKIE, refreshToken)
                    .secure(true)
                    .httpOnly(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(jwtComponent.getExpiredAfterFromToken(refreshToken).toEpochMilli() -
                            Instant.now().toEpochMilli())
                    .build();
            ResponseCookie deviceCookie = ResponseCookie.from(ParamName.DEVICE_ID_COOKIE, device.getDeviceId().toString())
                    .secure(true)
                    .httpOnly(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofDays(3650)) //10 year
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, deviceCookie.toString());
            var userRoomChat = userRoomChatRepository.existsByRoomChat_RoomNameAndUser(RoomChatName.GLOBAL.name(), user);
            if(!userRoomChat){
                roomChatRepository.findByRoomName(RoomChatName.GLOBAL.name())
                        .ifPresent(roomChat -> userRoomChatRepository.save(UserRoomChat.builder()
                                .roomChat(roomChat)
                                .user(user)
                                .build()));
            }
            getRedirectStrategy().sendRedirect(request, response, appProperties.getFrontendUrl() + "/home?login=true");
        } catch (Exception e) {
            log.error("Error while sending redirect to {}", request.getRequestURI(), e);
            response.sendRedirect(appProperties.getFrontendUrl() + "/auth/login");
        }
    }
}
