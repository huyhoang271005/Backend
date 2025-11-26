package com.example.hello.Users.User.Controller;

import com.example.hello.Users.User.DTO.ProfileRequest;
import com.example.hello.Users.User.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {
    ProfileService profileService;

    @PreAuthorize("hasAuthority('GET_PROFILE')")
    @GetMapping("profile")
    ResponseEntity<?> getUserByUserId(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PreAuthorize("hasAuthority('UPDATE_PROFILE')")
    @PutMapping(value = "profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> updateProfile(@Valid @RequestPart(name = "profileRequest") ProfileRequest profile,
                                    @RequestPart(required = false) MultipartFile avatar,
                                    @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(profileService.updateProfile(userId, profile, avatar));
    }
}
