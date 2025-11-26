package com.example.hello.Users.Authentication.Controller;

import com.example.hello.Infrastructure.Security.JwtProperties;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.Response;
import com.example.hello.Users.Authentication.DTO.EmailRequest;
import com.example.hello.Users.Authentication.DTO.LoginRequest;
import com.example.hello.Users.Authentication.DTO.LoginResponse;
import com.example.hello.Users.Authentication.DTO.PasswordRequest;
import com.example.hello.Infrastructure.Security.DeviceType;
import com.example.hello.Users.Authentication.Service.*;
import com.example.hello.Users.User.DTO.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@RequestMapping("auth")
public class AuthController {
    TokenService tokenService;
    RegisterService registerService;
    LoginService loginService;
    VerifyService verifyService;
    JwtProperties jwtProperties;

    @PostMapping("refresh-token")
    ResponseEntity<?> refreshToken(
            @RequestBody(required = false) LoginResponse loginResponse,
            @CookieValue(name = ParamName.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest request
    ) {
        DeviceType deviceType = (DeviceType) request.getAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE);
        Response<LoginResponse> responseRefresh;
        if(deviceType == DeviceType.WEB){
            responseRefresh = tokenService.refreshToken(refreshToken);
            ResponseCookie cookie = ResponseCookie.from(ParamName.REFRESH_TOKEN_COOKIE, responseRefresh.getData().getRefreshToken())
                    .secure(true)
                    .httpOnly(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(jwtProperties.getRefreshTokenSeconds())
                    .build();
            responseRefresh.getData().setRefreshToken(null);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(responseRefresh);
        }
        responseRefresh = tokenService.refreshToken(loginResponse.getRefreshToken());
        return ResponseEntity.ok().body(responseRefresh);
    }

    @PostMapping(value = "register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> register(
            HttpServletRequest request, @Valid @RequestPart RegisterRequest registerRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @CookieValue(name = ParamName.DEVICE_ID_COOKIE, required = false) UUID deviceId,
            @RequestHeader(ParamName.DEVICE_NAME_HEADER) String deviceName) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        DeviceType deviceType = (DeviceType) request.getAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE);
        var result = registerService.register(registerRequest, avatar, deviceId, userAgent, deviceType.name(), deviceName);
        if(deviceType == DeviceType.WEB){
            ResponseCookie cookie = ResponseCookie.from(ParamName.DEVICE_ID_COOKIE, result.getData().getDeviceId().toString())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(jwtProperties.getRefreshTokenSeconds())
                    .build();
            result.getData().setDeviceId(null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }


    @PostMapping("login")
    ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                            @CookieValue(name = ParamName.REFRESH_TOKEN_COOKIE, required = false) String oldRefreshToken,
                            @CookieValue(name = ParamName.DEVICE_ID_COOKIE, required = false) UUID deviceId,
                            HttpServletRequest request) {
        var loginResponse = loginService.login(loginRequest, oldRefreshToken, deviceId);
        DeviceType deviceType = (DeviceType) request.getAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE);
        if(loginResponse.getSuccess()){
            if(deviceType ==  DeviceType.WEB){
                ResponseCookie cookie = ResponseCookie.from(ParamName.REFRESH_TOKEN_COOKIE, loginResponse.getData().getRefreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .sameSite("None")
                        .maxAge(jwtProperties.getRefreshTokenSeconds())
                        .build();
                loginResponse.getData().setRefreshToken(null);
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(loginResponse);
            }
            return ResponseEntity.ok().body(loginResponse);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(loginResponse);
    }

    @PostMapping("send-verify-email")
    ResponseEntity<?> sendVerifyEmail (@Valid @RequestBody EmailRequest emailRequest) {
        return ResponseEntity.ok(verifyService.sendVerifyEmail(emailRequest));
    }

    @PostMapping("send-verify-device")
    ResponseEntity<?> sendVerifyDevice(
            HttpServletRequest request, @Valid @RequestBody EmailRequest emailRequest,
            @CookieValue(name = ParamName.DEVICE_ID_COOKIE, required = false) UUID deviceId,
            @RequestHeader(ParamName.DEVICE_NAME_HEADER)  String deviceName,
            @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        DeviceType deviceType = (DeviceType) request.getAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE);
        var response = verifyService.sendVerifyDevice(emailRequest, deviceId, userAgent, deviceType.name(), deviceName);
        if(deviceType == DeviceType.WEB){
            ResponseCookie cookie = ResponseCookie.from(ParamName.DEVICE_ID_COOKIE, response.getData().getDeviceId().toString())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(60*60*24*365*10)
                    .build();
            response.getData().setDeviceId(null);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("verify/{token}")
    ResponseEntity<?> verify(@PathVariable String token) {
        return ResponseEntity.ok(verifyService.verify(token));
    }

    @PostMapping("send-verify-change-password")
    ResponseEntity<?> sendVerifyChangePassword(@Valid @RequestBody EmailRequest emailRequest) {
        return ResponseEntity.ok(verifyService.sendVerifyChangePassword(emailRequest));
    }

    @PostMapping("verify-change-password")
    ResponseEntity<?> changePassword(@Valid @RequestBody PasswordRequest passwordRequest,
                                                  @RequestParam  String token) {
        return ResponseEntity.ok(verifyService.changePassword(passwordRequest, token));
    }

}
