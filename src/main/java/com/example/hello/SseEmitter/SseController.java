package com.example.hello.SseEmitter;

import com.example.hello.Enum.DeviceType;
import com.example.hello.Middleware.ParamName;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SseController {
    SseService sseService;

    @GetMapping(value = "sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(HttpServletRequest request,
                              @CookieValue(value = ParamName.REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        DeviceType deviceType = (DeviceType) request.getAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE);
        var refresh = deviceType == DeviceType.WEB ? refreshToken : request.getHeader("refresh-token");
        return sseService.createSseEmitter(refresh);
    }
}
