package com.example.hello.Users.User.Controller;

import com.example.hello.Middleware.ParamName;
import com.example.hello.Users.User.Service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionController {
    SessionService sessionService;

    @PreAuthorize("hasAuthority('LOGOUT_USER_ALL')")
    @GetMapping("logout-all/{userId}")
    ResponseEntity<?> logoutUserAll(@PathVariable UUID userId) {
        return ResponseEntity.ok(sessionService.logOutAllSession(userId));
    }

    @PreAuthorize("hasAuthority('LOGOUT_ALL')")
    @GetMapping("logout-all")
    ResponseEntity<?> logoutAll(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(sessionService.logOutAllSession(userId));
    }

    @PreAuthorize("hasAuthority('LOGOUT')")
    @GetMapping("logout")
    ResponseEntity<?> logout(HttpServletRequest request) {
        UUID sessionId = (UUID) request.getAttribute(ParamName.SESSION_ID_ATTRIBUTE);
        return ResponseEntity.ok(sessionService.logOutSession(sessionId));
    }
}
