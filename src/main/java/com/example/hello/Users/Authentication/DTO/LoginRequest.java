package com.example.hello.Users.Authentication.DTO;

import com.example.hello.Middleware.Constant;
import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @Pattern(regexp = Constant.VALIDATION.PASSWORD , message = StringApplication.FIELD.PASSWORD +  StringApplication.FIELD.INVALID)
    String password;
    @NotBlank(message = StringApplication.FIELD.EMAIL + StringApplication.FIELD.NOT_EMPTY)
    @Email(message = StringApplication.FIELD.EMAIL + StringApplication.FIELD.INVALID)
    String email;
}
