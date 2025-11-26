package com.example.hello.Users.User.Controller;

import com.example.hello.Users.Authentication.DTO.EmailRequest;
import com.example.hello.Users.User.Service.EmailService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {
    EmailService  emailService;

    @PreAuthorize("hasAuthority('ADD_EMAIL')")
    @PostMapping("email")
    ResponseEntity<?> addEmail(@Valid @RequestBody EmailRequest emailRequest,
                               @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(emailService.addEmail(userId, emailRequest));
    }

    @PreAuthorize("hasAuthority('ADD_EMAIL_USER')")
    @PostMapping("email/{userId}")
    ResponseEntity<?> extendEmail(@Valid @RequestBody EmailRequest emailRequest,
                                  @PathVariable UUID userId) {
        return ResponseEntity.ok(emailService.addEmail(userId, emailRequest));
    }

    @PreAuthorize("hasAuthority('DELETE_EMAIL')")
    @DeleteMapping("email/{emailId}")
    ResponseEntity<?> deleteEmail(@PathVariable UUID emailId) {
        return ResponseEntity.ok(emailService.deleteEmail(emailId));
    }
}
