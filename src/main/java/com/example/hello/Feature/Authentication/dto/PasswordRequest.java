package com.example.hello.Feature.Authentication.dto;

import com.example.hello.Infrastructure.Common.Validation.RegexValidation;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordRequest {
    @Pattern(regexp = RegexValidation.VALIDATION.PASSWORD , message = StringApplication.FIELD.PASSWORD +  StringApplication.FIELD.INVALID)
    String password;
}
