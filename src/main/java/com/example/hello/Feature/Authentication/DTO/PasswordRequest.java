package com.example.hello.Feature.Authentication.DTO;

import com.example.hello.Middleware.Constant;
import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordRequest {
    @Pattern(regexp = Constant.VALIDATION.PASSWORD , message = StringApplication.FIELD.PASSWORD +  StringApplication.FIELD.INVALID)
    String password;
}
