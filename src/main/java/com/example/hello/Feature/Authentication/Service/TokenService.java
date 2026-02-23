package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnauthorizedException;
import com.example.hello.Infrastructure.Jwt.JwtProperties;
import com.example.hello.Feature.Authentication.dto.LoginResponse;
import com.example.hello.Feature.User.dto.Address;
import com.example.hello.Mapper.SessionMapper;
import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.Enum.TokenName;
import com.example.hello.Entity.Token;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Infrastructure.Jwt.JwtComponent;
import com.example.hello.Feature.Authentication.Repository.TokenRepository;
import com.example.hello.Entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService {
    TokenRepository tokenRepository;
    SessionRepository sessionRepository;
    SessionCacheService sessionCacheService;
    JwtComponent jwtComponent;
    JwtProperties jwtProperties;
    private final SessionMapper sessionMapper;

    @Transactional
    public Response<LoginResponse> refreshToken(String refreshToken, String ip, Address address) {
        UUID userId;
        UUID sessionId;
        TokenName tokenName;
        try {
            //Khi refresh refreshToken còn hạn thì lấy dữ liệu từ refreshToken và xử lý tiếp
            userId = jwtComponent.getUserIdFromToken(refreshToken);
            sessionId = jwtComponent.getSessionIdFromToken(refreshToken);
            tokenName = jwtComponent.getTokenNameFromToken(refreshToken);
            log.info("Jwt can use");
        } catch (Exception e){
            //Tìm session thông qua refreshToken
            log.error("Jwt can not use (expired)");
            sessionRepository.findSessionByTokenValue(refreshToken).ifPresent(session -> {
                log.info("Revoked refreshToken");
                session.setRevoked(true);
                sessionCacheService.updateRevoked(session.getSessionId(), true);
            });
            throw new UnauthorizedException(e.getMessage());
        }
        //Đã exception khi refreshToken không phải refresh refreshToken
        if(tokenName != TokenName.REFRESH_TOKEN){
            log.error("Token name not refresh token");
            throw new UnprocessableEntityException(StringApplication.FIELD.REFRESH_TOKEN + StringApplication.FIELD.INVALID);
        }
        //Tìm session thông qua sessionId
        var session = sessionRepository.findById(sessionId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.NOT_EXIST)
        );
        //Nếu session bị thu hồi quyền đăng nhập đá exception
        if(session.getRevoked()){
            log.error("Session revoked");
            throw new UnauthorizedException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.EXPIRED);
        }
        //Lấy user
        User user = session.getUser();
        //Đối chiếu với userId trong refreshToken
        if(!user.getUserId().equals(userId)) {
            log.error("User id different in db");
            throw new ConflictException(StringApplication.FIELD.USER + StringApplication.FIELD.INVALID);
        }
        //Đối chiếu với refreshToken của session so với refreshToken user gửi lên
        Token userToken = tokenRepository.findBySessionAndTokenName(session, tokenName)
                .orElseThrow(() -> new ConflictException(StringApplication.ERROR.USER_NOT_LOGIN));
        if(!userToken.getTokenValue().equals(refreshToken)) {
            log.error("Token value different in db");
            log.info("Token client is {}", refreshToken);
            log.info("Token in db is {}", userToken.getTokenValue());
            throw new UnauthorizedException(StringApplication.FIELD.TOKEN + StringApplication.FIELD.INVALID);
        }
        if(!session.getIpAddress().equals(ip)) {
            log.info("Set new ip successfully");
            session.setIpAddress(ip);
            sessionMapper.updateSession(address, session);
            log.info("Set new address successfully");
            sessionRepository.save(session);
        }
        //Sinh access refreshToken và refresh refreshToken mới và trả về
        String newRefreshToken = jwtComponent.generateToken(userId, TokenName.REFRESH_TOKEN,
                session.getSessionId(), jwtComponent.getExpiredAfterFromToken(refreshToken));
        log.info("New refresh token generated");
        userToken.setTokenValue(newRefreshToken);
        String newAccessToken = jwtComponent.generateToken(userId, TokenName.ACCESS_TOKEN,
                session.getSessionId(), Instant.now().plusSeconds(jwtProperties.getAccessTokenSeconds()));
        log.info("New access token generated");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                LoginResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build()
        );
    }
}
