package com.example.hello.Users.Authentication.DTO;

import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    @NotBlank(message = StringApplication.FIELD.EMAIL + StringApplication.FIELD.NOT_EMPTY)
    @Email(message = StringApplication.FIELD.EMAIL + StringApplication.FIELD.INVALID)
    String email;
}
