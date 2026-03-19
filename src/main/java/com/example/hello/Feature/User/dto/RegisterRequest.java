package com.example.hello.Feature.User.dto;

import com.example.hello.Infrastructure.Common.Validation.RegexValidation;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import com.example.hello.Feature.Authentication.dto.LoginRequest;
import com.example.hello.Enum.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest extends LoginRequest {
    @Pattern(regexp = RegexValidation.VALIDATION.USERNAME, message = StringApplication.FIELD.USERNAME + StringApplication.FIELD.INVALID)
    String username;
    @Pattern(regexp = RegexValidation.VALIDATION.FULL_NAME, message = StringApplication.FIELD.FULL_NAME + StringApplication.FIELD.INVALID)
    String fullName;
    @NotNull
    @Past(message = StringApplication.FIELD.DATE + StringApplication.FIELD.INVALID)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday;
    Gender gender;
}
