package com.example.hello.Feature.User.DTO;

import com.example.hello.Middleware.Constant;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Enum.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileRequest {
    @Pattern(regexp = Constant.VALIDATION.USERNAME, message = StringApplication.FIELD.USERNAME + StringApplication.FIELD.INVALID)
    String username;
    @Pattern(regexp = Constant.VALIDATION.FULL_NAME, message = StringApplication.FIELD.FULL_NAME + StringApplication.FIELD.INVALID)
    String fullName;
    Gender gender;
    @NotNull
    @Past(message = StringApplication.FIELD.DATE + StringApplication.FIELD.INVALID)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday;
}
