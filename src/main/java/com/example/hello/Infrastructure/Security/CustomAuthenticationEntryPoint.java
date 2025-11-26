package com.example.hello.Infrastructure.Security;

import com.example.hello.Infrastructure.Exception.ErrorResponse;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        var res = new Response<>(
                false,
                StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID,
                new ErrorResponse(authException.getMessage())
        );
        String json = objectMapper.writeValueAsString(res);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(json);
        log.error(request.getRequestURL().toString(), authException.getMessage(),  authException);
    }
}
