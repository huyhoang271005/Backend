package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnauthorizedException;
import com.example.hello.Infrastructure.Jwt.JwtProperties;
import com.example.hello.Feature.Authentication.DTO.LoginResponse;
import com.example.hello.Repository.SessionRepository;
import com.example.hello.Enum.TokenName;
import com.example.hello.Entity.Token;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Infrastructure.Jwt.JwtComponent;
import com.example.hello.Repository.TokenRepository;
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

    @Transactional
    public Response<LoginResponse> refreshToken(String token) {
        //Đá exception khi token không được gửi
        if(token == null) {
            throw new UnauthorizedException(StringApplication.FIELD.TOKEN + StringApplication.FIELD.INVALID);
        }
        UUID userId;
        UUID sessionId;
        TokenName tokenName;
        try {
            //Khi refresh token còn hạn thì lấy dữ liệu từ token và xử lý tiếp
            userId = jwtComponent.getUserIdFromToken(token);
            sessionId = jwtComponent.getSessionIdFromToken(token);
            tokenName = jwtComponent.getTokenNameFromToken(token);
        } catch (Exception e){
            //Tìm session thông qua token
            var userToken = tokenRepository.findByTokenValue(token).orElseThrow(
                    ()-> new  EntityNotFoundException(StringApplication.FIELD.TOKEN + StringApplication.FIELD.NOT_EXIST)
            );
            var session = userToken.getSession();
            //Khi refresh token hết hạn thì thu hồi quyền đăng nhập của session
            sessionCacheService.updateRevoked(session.getSessionId(), true);
            session.setRevoked(true);
            sessionRepository.save(session);
            throw new UnauthorizedException(e.getMessage());
        }
        //Đã exception khi token không phải refresh token
        if(tokenName != TokenName.REFRESH_TOKEN){
            throw new ConflictException(StringApplication.FIELD.REFRESH_TOKEN + StringApplication.FIELD.INVALID);
        }
        //Tìm session thông qua sessionId
        var session = sessionRepository.findById(sessionId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.NOT_EXIST)
        );
        //Nếu session bị thu hồi quyền đăng nhập đá exception
        if(session.getRevoked()){
            throw new UnauthorizedException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.EXPIRED);
        }
        //Lấy user
        User user = session.getUser();
        //Đối chiếu với userId trong token
        if(!user.getUserId().equals(userId)) {
            throw new UnprocessableEntityException(StringApplication.FIELD.USER + StringApplication.FIELD.INVALID);
        }
        //Đối chiếu với token của session so với token user gửi lên
        Token userToken = tokenRepository.findBySessionAndTokenName(session, tokenName)
                .orElseThrow(() -> new ConflictException(StringApplication.ERROR.USER_NOT_LOGIN));
        if(!userToken.getTokenValue().equals(token)) {
            throw new ConflictException(StringApplication.FIELD.TOKEN + StringApplication.FIELD.INVALID);
        }
        //Sinh access token và refresh token mới và trả về
        String newRefreshToken = jwtComponent.generateToken(userId, TokenName.REFRESH_TOKEN,
                session.getSessionId(), jwtComponent.getExpiredAfterFromToken(token));
        userToken.setTokenValue(newRefreshToken);
        tokenRepository.save(userToken);
        String newAccessToken = jwtComponent.generateToken(userId, TokenName.ACCESS_TOKEN,
                session.getSessionId(), Instant.now().plusSeconds(jwtProperties.getAccessTokenSeconds()));
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
