package com.example.hello.Feature.Contact;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
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
    String phone;
    String address;
    Instant updatedAt;
}
