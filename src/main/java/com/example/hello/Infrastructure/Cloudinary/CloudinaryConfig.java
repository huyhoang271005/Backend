package com.example.hello.Infrastructure.Cloudinary;

import com.cloudinary.Cloudinary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    String cloudName;

    @Value("${cloudinary.api-key}")
    String apiKey;

    @Value("${cloudinary.api-secret}")
    String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
           "cloud_name", cloudName,
           "api_key", apiKey,
           "api_secret", apiSecret,
            "secure", true
        ));
    }
}
