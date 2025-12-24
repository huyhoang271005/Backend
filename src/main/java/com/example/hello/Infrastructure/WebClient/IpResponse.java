package com.example.hello.Infrastructure.WebClient;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IpResponse {
    String city;
    String region;
    String country;
    String timezone;
}
