package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Entity.UserRoomChat;
import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Jwt.JwtProperties;
import com.example.hello.Feature.Authentication.UserDetail.MyUserDetails;
import com.example.hello.Feature.Authentication.Repository.TokenRepository;
import com.example.hello.Feature.User.Repository.DeviceRepository;
import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.WebSocket.RoomChat.UserRoomChatRepository;
import com.example.hello.WebSocket.RoomChat.RoomChatRepository;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Infrastructure.Jwt.JwtComponent;
import com.example.hello.Feature.Authentication.dto.LoginRequest;
import com.example.hello.Feature.Authentication.dto.LoginResponse;
import com.example.hello.Entity.Email;
import com.example.hello.Entity.Token;
import com.example.hello.Enum.TokenName;
import com.example.hello.Entity.User;
import com.example.hello.WebSocket.RoomChat.RoomChatName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginService {
    AuthenticationManager authenticationManager;
    TokenRepository tokenRepository;
    DeviceRepository deviceRepository;
    SessionRepository sessionRepository;
    SessionCacheService sessionCacheService;
    JwtComponent jwtComponent;
    JwtProperties jwtProperties;
    UserRoomChatRepository userRoomChatRepository;
    RoomChatRepository roomChatRepository;

    @Transactional
    public Response<LoginResponse> login(LoginRequest loginRequest, String oldRefreshToken,
                                         UUID deviceId) {
        try {
            UUID idDevice;
            UUID sessionId;
            //Kiểm tra xem jwt còn hạn không
            try {
                //Còn hạn thì lấy deviceId từ jwt
                sessionId = jwtComponent.getSessionIdFromToken(oldRefreshToken);
                var session = sessionRepository.findById(sessionId).orElseThrow(()->
                        new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.NOT_EXIST));
                log.info("Session id from jwt {}", sessionId);
                idDevice = session.getDevice().getDeviceId();
                log.info("Device id from session {}", idDevice);
            } catch (Exception e){
                //Nếu hết hạn thì lấy deviceId dự phòng
                idDevice = deviceId;
                log.info("Device id from cookie or header {}", idDevice);
            }
            //Check bảo mật thông qua userDetail của spring
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            Email email = userDetails.email();
            //Lấy user từ email
            User user = email.getUser();
            log.info("User id is {}", user.getUserId());
            //Lấy session từ deviceId
            var session = Optional.ofNullable(idDevice)
                    .flatMap(deviceRepository::findById)
                    .flatMap(device -> sessionRepository.findByUserAndDevice(user, device))
                    .orElse(null);
            if(session == null || !session.getValidated()){
                //Nếu không có session hiện tại => Thiết bị mới
                if(session != null){
                    log.info("Session id is {}", session.getSessionId());
                    log.info("Session validated {}", session.getValidated());
                }else {
                    log.info("Session id is null");
                }
                return new Response<>(
                        true,
                        StringApplication.ERROR.NEW_DEVICE,
                        LoginResponse.builder()
                                .verifiedEmail(true)
                                .verifiedDevice(false)
                                .build()
                );
            }
            //Tạo refresh token và access token
            String refreshToken = jwtComponent.generateToken(user.getUserId(), TokenName.REFRESH_TOKEN,
                    session.getSessionId(),
                    Instant.now().plusSeconds(jwtProperties.getRefreshTokenSeconds()));
            log.info("Created refresh token");
            String accessToken = jwtComponent.generateToken(user.getUserId(), TokenName.ACCESS_TOKEN,
                    session.getSessionId(),
                    Instant.now().plusSeconds(jwtProperties.getAccessTokenSeconds()));
            log.info("Created access token");
            if(session.getRevoked()) {
                //Cho phép quyền đăng nhập cho session này
                log.info("Set revoked cache is false");
                sessionCacheService.updateRevoked(session.getSessionId(), false);
                session.setRevoked(false);
                log.info("Set revoked session is false");
            }
            //Check refresh token của user
            var userToken = tokenRepository.findBySessionAndTokenName(session, TokenName.REFRESH_TOKEN)
                    .orElseGet(()->
                            //Nếu không có refresh token nào => Session đăng nhập lần đầu
                            //=> Tạo refresh token mới
                            Token.builder()
                                    .session(session)
                                    .tokenName(TokenName.REFRESH_TOKEN)
                                    .build());
            userToken.setTokenValue(refreshToken);
            tokenRepository.save(userToken);
            log.info("Created user refresh token");
            var userRoomChat = userRoomChatRepository.existsByRoomChat_RoomNameAndUser(RoomChatName.GLOBAL.name(), user);
            if(!userRoomChat){
                roomChatRepository.findByRoomName(RoomChatName.GLOBAL.name())
                        .ifPresent(roomChat -> userRoomChatRepository.save(UserRoomChat.builder()
                                        .roomChat(roomChat)
                                        .user(user)
                                .build()));
            }
            return new Response<>(
                    true,
                    StringApplication.SUCCESS.LOGIN_SUCCESS,
                    new LoginResponse(true, true, accessToken, refreshToken)
            );
        }
        catch (BadCredentialsException e) {
            //Đá exception khi email hoặc password sai
            throw new ConflictException(StringApplication.ERROR.PASSWORD_OR_EMAIL_INCORRECT);
        }
        catch (UsernameNotFoundException e) {
            //Đá exception khi user không tồn tại
            throw new EntityNotFoundException(StringApplication.FIELD.USER +  StringApplication.FIELD.NOT_EXIST);
        }
        catch (LockedException e) {
            //Đá exception khi trạng thái user khác active
            throw new ConflictException(StringApplication.ERROR.CANT_LOGIN);
        }
        catch (DisabledException e) {
            //Trả dữ liệu về user khi email chưa được xác thực
            return new Response<>(
                    true,
                    StringApplication.FIELD.EMAIL + StringApplication.FIELD.UNVERIFIED,
                    LoginResponse.builder()
                            .verifiedEmail(false)
                            .build()
            );
        }
    }
}
