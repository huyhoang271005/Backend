package com.example.hello.Feature.User.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailResponse {
    UUID emailId;
    String email;
    Boolean validated;
}
