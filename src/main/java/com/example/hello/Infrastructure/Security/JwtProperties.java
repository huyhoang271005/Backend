package com.example.hello.Infrastructure.Security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt.expiration")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtProperties {
    long accessTokenSeconds;
    long refreshTokenSeconds;
}
