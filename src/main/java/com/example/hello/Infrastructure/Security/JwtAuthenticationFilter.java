package com.example.hello.Infrastructure.Security;

import com.example.hello.Infrastructure.Cache.RoleCacheService;
import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Cache.UserStatusCacheService;
import com.example.hello.Infrastructure.Exception.ErrorResponse;
import com.example.hello.Infrastructure.Jwt.JwtComponent;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Infrastructure.Cache.RolePermissionCacheService;
import com.example.hello.Enum.TokenName;
import com.example.hello.Enum.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    JwtComponent jwtComponent;
    ObjectMapper objectMapper;
    RolePermissionCacheService rolePermissionCacheService;
    RoleCacheService roleCacheService;
    UserStatusCacheService userStatusCacheService;
    SessionCacheService sessionCacheService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                UUID userId = jwtComponent.getUserIdFromToken(token);
                UUID sessionId = jwtComponent.getSessionIdFromToken(token);
                TokenName tokenName = jwtComponent.getTokenNameFromToken(token);
                UserStatus userStatus = userStatusCacheService.getUserStatus(userId);
                log.info("Access token can use");
                if(!tokenName.equals(TokenName.ACCESS_TOKEN)){
                    log.error("Token not is access token");
                    var res = new Response<>(
                            false,
                            StringApplication.FIELD.ACCESS_TOKEN + StringApplication.FIELD.INVALID,
                            null
                    );
                    String json = objectMapper.writeValueAsString(res);
                    response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                    response.getWriter().print(json);
                    return;
                }
                if(sessionCacheService.getRevoked(sessionId)){
                    log.error("User revoked session");
                    var res = new Response<>(
                            false,
                            StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.EXPIRED,
                            null
                    );
                    String json = objectMapper.writeValueAsString(res);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().print(json);
                    return;
                }
                String userStatusString = switch (userStatus) {
                    case LOCKED -> StringApplication.ERROR.ACCOUNT_LOCKED;
                    case PENDING -> StringApplication.ERROR.ACCOUNT_PENDING;
                    default -> null;
                };
                if(userStatus != UserStatus.ACTIVE){
                    log.error("User not active");
                    var res = new Response<>(
                            false,
                            userStatusString,
                            null
                    );
                    String json = objectMapper.writeValueAsString(res);
                    response.setStatus(HttpStatus.CONFLICT.value());
                    response.getWriter().print(json);
                    return;
                }
                request.setAttribute(ParamName.SESSION_ID_ATTRIBUTE, sessionId);
                // Lưu vào SecurityContext
                var authorities = rolePermissionCacheService.getPermissionsCache(roleCacheService.getRoleCache(userId))
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authToken.setDetails(tokenName);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                log.error("Access token expired");
                var res = new Response<>(
                        false,
                        StringApplication.FIELD.TOKEN +  StringApplication.FIELD.INVALID,
                        new ErrorResponse(e.getMessage())
                );
                String json = objectMapper.writeValueAsString(res);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().print(json);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
