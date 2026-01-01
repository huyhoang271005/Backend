package com.example.hello.Feature.User.Controller;

import com.example.hello.Feature.User.DTO.ExtendUserRequest;
import com.example.hello.Feature.User.Service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class UserController {
    UserService userService;


    @GetMapping("users")
    ResponseEntity<?> getUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @GetMapping("users/{userId}")
    ResponseEntity<?> getUserById(@PathVariable UUID userId,
                                  @AuthenticationPrincipal UUID myId) {
        return ResponseEntity.ok(userService.getUser(userId, myId));
    }

    @PreAuthorize("hasAuthority('GET_USER_STATUS')")
    @GetMapping("user-status")
    ResponseEntity<?> getUserStatus() {
        return ResponseEntity.ok(userService.getUserStatuses());
    }

    @PreAuthorize("hasAuthority('UPDATE_USER_ADMIN')")
    @PatchMapping("users")
    ResponseEntity<?> updateUser(@RequestBody ExtendUserRequest extendUserRequest){
        return ResponseEntity.ok(userService.updateUser(extendUserRequest));
    }

    @GetMapping("home")
    ResponseEntity<?> getHome(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(userService.getHome(userId));
    }
}
