package com.example.hello.Infrastructure.Security;

import com.example.hello.Infrastructure.Cache.RoleCache;
import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Cache.UserStatusCacheService;
import com.example.hello.Infrastructure.Exception.ErrorResponse;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.example.hello.Infrastructure.Cache.RolePermissionCacheService;
import com.example.hello.Users.User.Enum.TokenName;
import com.example.hello.Users.User.Enum.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    JwtService jwtService;
    ObjectMapper objectMapper;
    RolePermissionCacheService rolePermissionCacheService;
    RoleCache roleCache;
    UserStatusCacheService userStatusCacheService;
    SessionCacheService sessionCacheService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                UUID userId = jwtService.getUserIdFromToken(token);
                UUID sessionId = jwtService.getSessionIdFromToken(token);
                TokenName tokenName = jwtService.getTokenNameFromToken(token);
                UserStatus userStatus = userStatusCacheService.getUserStatus(userId);
                if(!tokenName.equals(TokenName.ACCESS_TOKEN)){
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
                var authorities = rolePermissionCacheService.getPermissionsCache(roleCache.getRoleCache(userId))
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authToken.setDetails(tokenName);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
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
