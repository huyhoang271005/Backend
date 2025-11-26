package com.example.hello.Users.User.Controller;

import com.example.hello.Users.User.DTO.ExtendUserRequest;
import com.example.hello.Users.User.Service.UserService;
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
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class UserController {
    UserService userService;


    @PreAuthorize("hasAuthority('GET_USERS')")
    @GetMapping("users")
    ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PreAuthorize("hasAuthority('GET_USER')")
    @GetMapping("user/{userId}")
    ResponseEntity<?> getUserById(@PathVariable UUID userId,
                                  @AuthenticationPrincipal UUID myId) {
        return ResponseEntity.ok(userService.getUser(userId, myId));
    }

    @PreAuthorize("hasAuthority('GET_USER_STATUS')")
    @GetMapping("user-status")
    ResponseEntity<?> getUserStatus() {
        return ResponseEntity.ok(userService.getUserStatuses());
    }

    @PreAuthorize("hasAuthority('UPDATE_USER_EXTEND')")
    @PostMapping("user")
    ResponseEntity<?> updateUser(@RequestBody ExtendUserRequest extendUserRequest){
        return ResponseEntity.ok(userService.updateUser(extendUserRequest));
    }
}
