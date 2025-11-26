package com.example.hello.Infrastructure.Security;

import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        var responseObject = new Response<>(
                false,
                StringApplication.ERROR.FORBIDDEN,
                Map.of("error", accessDeniedException.getMessage())
        );
        String json = objectMapper.writeValueAsString(responseObject);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().print(json);
        log.error(request.getRequestURL().toString(), accessDeniedException.getMessage());
    }
}
