package com.example.hello.Feature.User.Controller;

import com.example.hello.Feature.User.dto.ProfileRequest;
import com.example.hello.Feature.User.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("profile")
public class ProfileController {
    ProfileService profileService;

    @GetMapping
    ResponseEntity<?> getUserByUserId(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> updateProfile(@Valid @RequestPart(name = "profileRequest") ProfileRequest profile,
                                    @RequestPart(required = false) MultipartFile avatar,
                                    @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(profileService.updateProfile(userId, profile, avatar));
    }
}
