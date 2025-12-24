package com.example.hello.Feature.User.DTO;

import com.example.hello.Middleware.Constant;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.Authentication.DTO.LoginRequest;
import com.example.hello.Enum.Gender;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest extends LoginRequest {
    @Pattern(regexp = Constant.VALIDATION.USERNAME, message = StringApplication.FIELD.USERNAME + StringApplication.FIELD.INVALID)
    String username;
    @Pattern(regexp = Constant.VALIDATION.FULL_NAME, message = StringApplication.FIELD.FULL_NAME + StringApplication.FIELD.INVALID)
    String fullName;
    @Pattern(regexp = Constant.VALIDATION.DATE, message = StringApplication.FIELD.DATE + StringApplication.FIELD.INVALID)
    String birthday;
    Gender gender;
}
