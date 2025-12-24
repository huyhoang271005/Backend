package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Jwt.JwtProperties;
import com.example.hello.Feature.Authentication.UserDetail.MyUserDetails;
import com.example.hello.Repository.DeviceRepository;
import com.example.hello.Repository.SessionRepository;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Infrastructure.Jwt.JwtComponent;
import com.example.hello.Feature.Authentication.DTO.LoginRequest;
import com.example.hello.Feature.Authentication.DTO.LoginResponse;
import com.example.hello.Repository.TokenRepository;
import com.example.hello.Entity.Email;
import com.example.hello.Entity.Token;
import com.example.hello.Enum.TokenName;
import com.example.hello.Entity.User;
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
                idDevice = session.getDevice().getDeviceId();
            } catch (Exception e){
                //Nếu hết hạn thì lấy deviceId dự phòng
                idDevice = deviceId;
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
            //Lấy session từ deviceId
            var session = Optional.ofNullable(idDevice)
                    .flatMap(deviceRepository::findById)
                    .flatMap(device -> sessionRepository.findByUserAndDevice(user, device))
                    .orElse(null);
            if(session == null){
                //Nếu không có session hiện tại => Thiết bị mới
                return new Response<>(
                        true,
                        StringApplication.ERROR.NEW_DEVICE,
                        LoginResponse.builder()
                                .verifiedEmail(true)
                                .verifiedDevice(false)
                                .build()
                );
            }
            if(!session.getValidated()){
                //Nếu session chưa được xác thực
                throw new ConflictException(StringApplication.FIELD.DEVICE + StringApplication.FIELD.UNVERIFIED);
            }
            //Tạo refresh token và access token
            String refreshToken = jwtComponent.generateToken(user.getUserId(), TokenName.REFRESH_TOKEN,
                    session.getSessionId(),
                    Instant.now().plusSeconds(jwtProperties.getRefreshTokenSeconds()));
            String accessToken = jwtComponent.generateToken(user.getUserId(), TokenName.ACCESS_TOKEN,
                    session.getSessionId(),
                    Instant.now().plusSeconds(jwtProperties.getAccessTokenSeconds()));
            if(session.getRevoked()) {
                //Cho phép quyền đăng nhập cho session này
                sessionCacheService.updateRevoked(session.getSessionId(), false);
                session.setRevoked(false);
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
