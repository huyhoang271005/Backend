package com.example.hello.Feature.User.Controller;

import com.example.hello.Middleware.ParamName;
import com.example.hello.Feature.User.Service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionController {
    SessionService sessionService;

    @GetMapping("sessions")
    ResponseEntity<?> sessions(HttpServletRequest request, Pageable pageable,
                               @AuthenticationPrincipal UUID userId) {
        UUID mySessionId = (UUID) request.getAttribute(ParamName.SESSION_ID_ATTRIBUTE);
        return ResponseEntity.ok(sessionService.getSessions(userId, mySessionId, pageable));
    }

    @DeleteMapping("sessions/{sessionId}")
    ResponseEntity<?> deleteSession(@PathVariable UUID sessionId, @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(sessionService.deleteSession(userId, sessionId));
    }

    @PreAuthorize("hasAuthority('LOGOUT_USER_ALL')")
    @GetMapping("logout-all/{userId}")
    ResponseEntity<?> logoutUserAll(@PathVariable UUID userId) {
        return ResponseEntity.ok(sessionService.logOutAllSession(userId, null));
    }

    @GetMapping("logout-all")
    ResponseEntity<?> logoutAll(HttpServletRequest request, @AuthenticationPrincipal UUID userId) {
        UUID mySession = (UUID) request.getAttribute(ParamName.SESSION_ID_ATTRIBUTE);
        return ResponseEntity.ok(sessionService.logOutAllSession(userId, mySession));
    }

    @GetMapping("logout/{sessionId}")
    ResponseEntity<?> logoutSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(sessionService.logOutSession(sessionId));
    }

    @GetMapping("logout")
    ResponseEntity<?> logout(HttpServletRequest request) {
        UUID sessionId = (UUID) request.getAttribute(ParamName.SESSION_ID_ATTRIBUTE);
        return ResponseEntity.ok(sessionService.logOutSession(sessionId));
    }
}
