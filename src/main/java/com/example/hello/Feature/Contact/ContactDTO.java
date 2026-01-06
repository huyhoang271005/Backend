package com.example.hello.Feature.Contact;

import com.example.hello.Middleware.Constant;
import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContactDTO {
    UUID contactId;
    String contactName;
    @Pattern(regexp = Constant.VALIDATION.PHONE, message = StringApplication.ERROR.PHONE)
    String phone;
    String address;
    Instant updatedAt;
}
