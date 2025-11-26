package com.example.hello.Users.User.DTO;

import com.example.hello.Middleware.Constant;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Users.User.Enum.Gender;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    Gender genderName;
    @Pattern(regexp = Constant.VALIDATION.DATE, message = StringApplication.FIELD.DATE + StringApplication.FIELD.INVALID)
    String birthday;
}
