package com.example.hello.Feature.Contact;

import com.example.hello.Infrastructure.Common.Validation.RegexValidation;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
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
    @Pattern(regexp = RegexValidation.VALIDATION.PHONE, message = StringApplication.ERROR.PHONE)
    String phone;
    String address;
    Instant updatedAt;
}
